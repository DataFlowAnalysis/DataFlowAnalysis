package org.dataflowanalysis.analysis.resource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.Entity;

public abstract class ResourceProvider {
	protected ResourceSet resources = new ResourceSetImpl();
	
	/**
	 * Loads the required resources
	 */
	public abstract void loadRequiredResources();
	
	/**
	 * Determines, whether the resource loader has sufficient resources to run the analysis
	 * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method returns false
	 */
	public abstract boolean sufficientResourcesLoaded();

	/**
	 * Looks up an ECore element with the given class type
	 * @param id  Id of the objects that the lookup should return
	 * @return Returns the object with the given id
	 */
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

	/**
	 * Finds an element that satisfies the given condition
	 * @param condition Condition the element should satisfy
	 * @return Returns the first element found that satisfies the given condition
	 */
	public Optional<EObject> lookupElementWithCondition(Predicate<EObject> condition) {
		for (Resource resource : this.getResources()) {
			Optional<EObject> result = this.findInResource(condition, resource);	
            if (result.isPresent()) {
            	return result;
            }
        }
		return Optional.empty();
	}

	
	/**
	 * Collects all loaded resources loaded by the resource provider
	 * @return Returns a list of all loaded resources
	 */
	public Collection<Resource> getResources() {
		return new ArrayList<>(this.resources.getResources());
	}
	
	
	/**
	 * Looks up an ECore element with the given class type
	 * @param <T> Type of the objects that the lookup should return
	 * @param targetType Target type of the lookup
	 * @return Returns a list of objects that are of the target type
	 */
	public <T extends EObject> List<T> lookupToplevelElement(EClass targetType) {
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
	protected EObject loadModelContent(URI modelURI) {
		Resource resource = resources.getResource(modelURI, true);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded", modelURI));
		} else if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException(String.format("Model with URI %s is empty", modelURI));
		}
		return resource.getContents().get(0);
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
    		if (condition.test(top)) {
    			return Optional.of(top);
    		}
        	visitedNodes.put(top, true);
        }
        return Optional.empty();
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
