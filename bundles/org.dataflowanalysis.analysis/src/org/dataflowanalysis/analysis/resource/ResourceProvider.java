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
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.Entity;

/**
 * This class is responsible for accessing and loading resources for a {@link DataFlowConfidentialityAnalysis}. The
 * method {@link ResourceProvider#loadRequiredResources()} loads the requires resources and saves them into
 * {@link ResourceProvider#resources}. The method {@link ResourceProvider#sufficientResourcesLoaded()} indicates whether
 * the resource provider has loaded enough resources that an analysis could be run Lastly, the class contains multiple
 * methods for working with loaded resources, like finding specific model elements.
 */
public abstract class ResourceProvider {
    protected final ResourceSet resources = new ResourceSetImpl();

    /**
     * Loads the required resources for a {@link DataFlowConfidentialityAnalysis}. The loaded resources should be saved into
     * {@link ResourceProvider#resources}
     */
    public abstract void loadRequiredResources();

    /**
     * Determines, whether the resource loader has sufficient resources to run an analysis
     * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method
     * returns false
     */
    public abstract boolean sufficientResourcesLoaded();

    /**
     * Looks up an ECore element with the given entity id
     * @param id Identifier of the object that the lookup should return
     * @return Returns the object with the given identifier. As that element may not exist, the result is wrapped in an
     * {@link Optional}
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
     * Finds an ECore element that satisfies the given condition
     * @param condition Condition the ECore element should satisfy
     * @return Returns the first element found that satisfies the given condition. As an element that satisfies the
     * condition may not exist, the result is wrapped in an {@link Optional}
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
     * Looks up an ECore element with the given {@link EClass} type
     * @param <T> Type of the objects that the lookup should return
     * @param targetType Target {@link EClass} type of the lookup
     * @return Returns a list of objects that are of the target type
     */
    public <T extends EObject> List<T> lookupToplevelElement(EClass targetType) {
        ArrayList<T> result = new ArrayList<>();
        for (Resource resource : this.resources.getResources()) {
            if (this.isTargetInResource(targetType, resource)) {
                result.addAll(EcoreUtil.<T>getObjectsByType(resource.getContents(), targetType));
            }
        }
        return result;
    }

    /**
     * Loads the model content with the given {@link URI}
     * @param modelURI URI of the model that should be loaded
     * @return ECore object that is saved in the resource with the given URI
     * @throws IllegalArgumentException The model with the given URI could not be loaded or is empty
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
     * @return Returns the first entity, that fulfills the condition. As an element satisfying the condition may not exists,
     * the result is wrapped in an {@link Optional}
     */
    private Optional<EObject> findInResource(Predicate<EObject> condition, Resource resource) {
        if (resource == null) {
            return Optional.empty();
        }

        HashMap<EObject, Boolean> visitedNodes = new HashMap<>();
        Deque<EObject> stack = new ArrayDeque<>();
        stack.addAll(resource.getContents());

        while (!stack.isEmpty()) {
            EObject top = stack.pop();
            stack.addAll(top.eContents().stream().filter(it -> !(visitedNodes.containsKey(it) && visitedNodes.get(it))).collect(Collectors.toList()));

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
     * @return Returns true, if one element with the target type could be found in the resource. Otherwise, the method
     * returns false
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

    /**
     * Collects all loaded resources loaded by the resource provider
     * @return Returns a list of all loaded resources
     */
    public Collection<Resource> getResources() {
        return new ArrayList<>(this.resources.getResources());
    }
}
