package org.palladiosimulator.dataflow.confidentiality.analysis.resource;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public interface ResourceProvider {
	/**
	 * Loads the required resources
	 */
	public void loadRequiredResources();
	
	/**
	 * Returns the usage model that the resource loader has loaded
	 * @return Usage model saved in the resources
	 */
	public UsageModel getUsageModel();
	
	/**
	 * Returns the allocation model that the resource loader has loaded
	 * @return Allocation model saved in the resources
	 */
	public Allocation getAllocation();
	
	/**
	 * Looks up an ECore element with the given class type
	 * @param <T> Type of the objects that the lookup should return
	 * @param targetType Target type of the lookup
	 * @return Returns a list of objects that are of the target type
	 */
	public <T extends EObject> List<T> lookupElementOfType(final EClass targetType);
	
	/**
	 * Collects all loaded resources loaded by the resource provider
	 * @return Returns a list of all loaded resources
	 */
	public Collection<Resource> getResources();
	
	/**
	 * Determines, whether the resource loader has sufficient resources to run the analysis
	 * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method returns false
	 */
	public default boolean sufficientResourcesLoaded() {
		if (this.getUsageModel() == null || this.getAllocation() == null) {
			return false;
		}
		if (this.lookupElementOfType(RepositoryPackage.eINSTANCE.getRepository()).isEmpty()) {
			return false;
		}
		if (this.lookupElementOfType(SystemPackage.eINSTANCE.getSystem()).isEmpty()) {
			return false;
		}
		if (this.lookupElementOfType(ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment()).isEmpty()) {
			return false;
		}
		return true;
	}
	
	/**
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

	/**
	 * Looks up an ECore element with the given class type
	 * @param id  Id of the objects that the lookup should return
	 * @return Returns the object with the given id
	 */
	public default Optional<EObject> lookupElementWithId(String id) {
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
	


	/**
	/**
	 * Finds an element that satisfies the given condition
	 * @param condition Condition the element should satisfy
	 * @return Returns the first element found that satisfies the given condition
	 */
	public default Optional<EObject> findElement(Predicate<EObject> condition) {
		for (Resource resource : this.getResources()) {
			Optional<EObject> result = this.findInResource(condition, resource);	
            if (result.isPresent()) {
            	return result;
            }
        }
		return Optional.empty();
	}
}
