package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMURIResourceLoader implements PCMResourceLoader {
	private ResourceSet resources = new ResourceSetImpl();
	
	private URI usageModelURI;
	private URI allocationModelURI;
	private UsageModel usageModel;
	private Allocation allocation;
	
	public PCMURIResourceLoader(URI usageModelURI, URI allocationModelURI) {
		this.usageModelURI = usageModelURI;
		this.allocationModelURI = allocationModelURI;
	}

	@Override
	public void loadRequiredResources() {
		this.usageModel = (UsageModel) this.loadModelContent(usageModelURI);
		this.allocation = (Allocation) this.loadModelContent(allocationModelURI);
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

	private EObject loadModelContent(URI modelURI) {
		Resource resource = resources.getResource(modelURI, true);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded", modelURI));
		} else if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException(String.format("Model with URI %s is empty", modelURI));
		}
		return resource.getContents().get(0);
	}

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
	
}
