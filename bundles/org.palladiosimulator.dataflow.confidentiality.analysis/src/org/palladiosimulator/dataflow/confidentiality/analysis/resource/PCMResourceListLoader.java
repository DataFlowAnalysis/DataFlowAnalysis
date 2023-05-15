package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

public class PCMResourceListLoader implements ResourceLoader {
	private List<Resource> resources;
	private ResourceSet resourceSet;
	
	public PCMResourceListLoader(List<Resource> resources) {
		this.resources = resources;
	}

	@Override
	public void loadRequiredResources() {
		resourceSet.getResources().addAll(resources);
	}

	@Override
	public UsageModel getUsageModel() {
		for (Resource resource : this.resources) {
			if (this.isTargetInResource(UsagemodelPackage.eINSTANCE.getUsageModel(),
					resource)) {
				return (UsageModel) resource.getContents().get(0);
			}
		}
		throw new IllegalStateException("Resources do not contain a usage model");
	}

	@Override
	public Allocation getAllocation() {
		for (Resource resource : this.resources) {
			if (this.isTargetInResource(AllocationPackage.eINSTANCE.getAllocation(),
					resource)) {
				return (Allocation) resource.getContents().get(0);
			}
		}
		throw new IllegalStateException("Resources do not contain a allocation");
	}
	
	@Override
	public <T extends EObject> List<T> lookupElementOfType(EClass targetType) {
		ArrayList<T> result = new ArrayList<T>();
        for (Resource resource : this.resources) {
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
    }
}
