package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMURIResourceProvider implements ResourceProvider {
	private ResourceSet resources = new ResourceSetImpl();
	
	private URI usageModelURI;
	private URI allocationModelURI;
	private Optional<URI> nodeCharacteristicURI;
	private UsageModel usageModel;
	private Allocation allocation;
	
	/**
	 * Creates a new resource loader with the given model URIs
	 * @param usageModelURI URI to the usage model
	 * @param allocationModelURI URI to the allocation model
	 * @param nodeCharacteristicsURI URI to the node characteristics model
	 */
	public PCMURIResourceProvider(URI usageModelURI, URI allocationModelURI, Optional<URI> nodeCharacteristicsURI) {
		this.usageModelURI = usageModelURI;
		this.allocationModelURI = allocationModelURI;
		this.nodeCharacteristicURI = nodeCharacteristicsURI;
	}

	@Override
	public void loadRequiredResources() {
		this.usageModel = (UsageModel) this.loadModelContent(usageModelURI);
		this.allocation = (Allocation) this.loadModelContent(allocationModelURI);
		if (this.nodeCharacteristicURI.isPresent()) {
			this.loadModelContent(this.nodeCharacteristicURI.get());
		}
		List<Resource> loadedResources = null;
		do {
			loadedResources = new ArrayList<>(this.resources.getResources());
			loadedResources.forEach(it->EcoreUtil.resolveAll(it));
		} while (loadedResources.size() != this.resources.getResources().size());
	}

	@Override
	public UsageModel getUsageModel() {
		return this.usageModel;
	}

	@Override
	public Allocation getAllocation() {
		return this.allocation;
	}
	
	@Override
	public <T extends EObject> List<T> lookupElementOfType(EClass targetType) {
		ArrayList<T> result = new ArrayList<T>();
        for (Resource resource : this.resources.getResources()) {
            if (this.isTargetInResource(targetType, resource)) {
                result.addAll(EcoreUtil.<T> getObjectsByType(resource.getContents(), targetType));
            }
        }
        return result;
	}

	/**
	 * Loads the model content with the given URI
	 * @param modelURI URI of the model that should be loaded
	 * @return ECore object that is saved in the resource with the given URI
	 */
	private EObject loadModelContent(URI modelURI) {
		Resource resource = resources.getResource(modelURI, true);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded", modelURI));
		} else if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException(String.format("Model with URI %s is empty", modelURI));
		}
		return resource.getContents().get(0);
	}

	/**
	 * Determines whether a ECore type is present in the resource
	 * @param targetType ECore type that should be searched
	 * @param resource Resource that should be searched
	 * @return Returns true, if one element with the target type could be found in the resource.
	 * Otherwise, the method returns false
	 */
	private boolean isTargetInResource(EClass targetType, Resource resource) {
        if (resource != null) {
            for (EObject c : resource.getContents()) {
                if (targetType.isSuperTypeOf(c.eClass())) {
                    return true;
                }
            }
        }
        return false;
    }
	
	private Entity findInResource(String targetId, Resource resource) {
		if (resource == null) {
			return null;
		}
		
		HashMap<EObject, Boolean> visitedNodes = new HashMap<>();
		Deque<EObject> stack = new ArrayDeque<>();
		stack.addAll(resource.getContents());
		
        while(!stack.isEmpty()) {
        	EObject top = stack.pop();
        	stack.addAll(top.eContents().stream()
        			.filter(it -> !(visitedNodes.containsKey(it) && visitedNodes.get(it)))
        			.collect(Collectors.toList()));

    		if (visitedNodes.containsKey(top) && visitedNodes.get(top)) {
    			continue;
    		}
    		if (!(top instanceof Entity)) {
    			continue;
    		}
    		Entity entity = (Entity) top;
    		if (entity.getId().equals(targetId)) {
    			return entity;
    		}
        	visitedNodes.put(top, true);
        }
        return null;
    }

	@Override
	public Entity lookupElementWithId(String id) {
		for (Resource resource : this.resources.getResources()) {
			Entity result = this.findInResource(id, resource);	
            if (result != null) {
            	return result;
            }
        }
		return null;
	}
}
