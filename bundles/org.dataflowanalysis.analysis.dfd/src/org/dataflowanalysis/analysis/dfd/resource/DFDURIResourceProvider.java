package org.dataflowanalysis.analysis.dfd.resource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.Entity;

public class DFDURIResourceProvider implements DFDResourceProvider {
	private ResourceSet resources = new ResourceSetImpl();
	
	private URI dataFlowDiagramURI;
	private URI dataDictionaryURI;
	private DataFlowDiagram dataFlowDiagram;
	private DataDictionary dataDictionary;
	
	/**
	 * Creates a new resource loader with the given model URIs
	 * @param dataFlowDiagramURI URI to the data flow diagram model
	 * @param dataDictionaryURI URI to the data dictionary model
	 */
	public DFDURIResourceProvider(URI dataFlowDiagramURI, URI dataDictionaryURI) {
		this.dataFlowDiagramURI = dataFlowDiagramURI;
		this.dataDictionaryURI = dataDictionaryURI;
	}

	@Override
	public void loadRequiredResources() {
		this.dataFlowDiagram = (DataFlowDiagram) this.loadModelContent(dataFlowDiagramURI);
		this.dataDictionary = (DataDictionary) this.loadModelContent(dataDictionaryURI);
		List<Resource> loadedResources = null;
		do {
			loadedResources = new ArrayList<>(this.resources.getResources());
			loadedResources.forEach(it->EcoreUtil.resolveAll(it));
		} while (loadedResources.size() != this.resources.getResources().size());
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

	@Override
	public Collection<Resource> getResources() {
		return new ArrayList<>(this.resources.getResources());
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
    }/**
	 * Finds an element that fulfills the given condition in a given resource
	 * @param condition Condition the element should fulfill
	 * @param resource Resource that should be searched
	 * @return Returns the first entity, that fulfills the condition. If none are found, the method returns null
	 */
	private Optional<EObject> findInResource(Predicate<EObject> condition, Resource resource) {
		if (resource == null) {
			return Optional.empty();
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
    		if (condition.test(entity)) {
    			return Optional.of(entity);
    		}
        	visitedNodes.put(top, true);
        }
        return Optional.empty();
    }

	@Override
	public Optional<EObject> findElement(Predicate<EObject> condition) {
		for (Resource resource : this.getResources()) {
			Optional<EObject> result = this.findInResource(condition, resource);	
            if (result.isPresent()) {
            	return result;
            }
        }
		return Optional.empty();
	}

	@Override
	public Optional<EObject> lookupElementWithId(String id) {
		for (Resource resource : this.getResources()) {
			Optional<EObject> result = this.findInResource(it -> {
				if (it instanceof Entity) {
					return ((Entity) it).getId().equals(id);
				}
				return false;
			}, resource);
            if (result.isPresent()) {
            	return result;
            }
        }
		return Optional.empty();
	}

	@Override
	public DataFlowDiagram getDataFlowDiagram() {
		return this.dataFlowDiagram;
	}

	@Override
	public DataDictionary getDataDictionary() {
		return this.dataDictionary;
	}
}
