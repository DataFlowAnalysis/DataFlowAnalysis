package org.dataflowanalysis.analysis.pcm.core;

import java.util.ArrayList;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.dataflowanalysis.pcm.extension.model.confidentiality.characteristics.EnumCharacteristic;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AbstractAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.AssemblyAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.Assignments;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsFactory;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.NodeCharacteristicsPackage;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.ResourceAssignee;
import org.dataflowanalysis.pcm.extension.nodecharacteristics.nodecharacteristics.UsageAssignee;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.CompositeComponent;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

public class PCMVertexCharacteristicsCalculator {
    private final Logger logger = Logger.getLogger(PCMVertexCharacteristicsCalculator.class);
    private final ResourceProvider resourceLoader;

    /**
     * Creates a new node characteristic calculator with the given resource provider
     * @param resourceProvider Resource provider that is used to calculate characteristics for each node
     */
    public PCMVertexCharacteristicsCalculator(ResourceProvider resourceProvider) {
        this.resourceLoader = resourceProvider;
    }

    /**
     * Determines the node characteristics at a given node in the given stack of assembly contexts
     * @param node Node of which the data characteristics should be calculated
     * @param context Assembly context in which the node is contained
     * @return Returns a list of vertex characteristics that are applied to the given vertex
     */
    public List<CharacteristicValue> getVertexCharacteristics(Entity node, Deque<AssemblyContext> context) {
        return this.getVertexCharacteristics(node, context, new IdentityHashMap<>());
    }

    /**
     * Determines the vertex characteristics at a given node in the given stack of assembly contexts.
     * <p/>
     * Furthermore, assignments in the model will be replaced according to the given map of replacements
     * @param node Node of which the data characteristics should be calculated
     * @param context Assembly context in which the node is contained
     * @param replacements Map of replacements that replaces possible assignments in the model
     * @return Returns a list of vertex characteristics that are applied to the given vertex
     */
    public List<CharacteristicValue> getVertexCharacteristics(Entity node, Deque<AssemblyContext> context,
            Map<AbstractAssignee, AbstractAssignee> replacements) {
        Assignments assignments = this.resolveAssignments();
        replacements.keySet()
                .forEach(assignee -> assignments.getAssignee()
                        .remove(assignee));
        replacements.values()
                .forEach(assignee -> assignments.getAssignee()
                        .add(assignee));
        List<AbstractAssignee> assignees;
        if (node instanceof AbstractUserAction) {
            assignees = this.getUsageNodeAssignments(node, assignments);
        } else if (node instanceof AbstractAction) {
            assignees = this.getSEFFNodeAssignments(assignments, context);
        } else {
            logger.error(String.format("Requested node characteristics of unknown entity %s", node));
            throw new IllegalArgumentException();
        }
        List<EnumCharacteristic> enumCharacteristics = assignees.stream()
                .flatMap(it -> it.getCharacteristics()
                        .stream())
                .toList();

        return enumCharacteristics.stream()
                .flatMap(it -> it.getValues()
                        .stream()
                        .map(val -> new PCMCharacteristicValue(it.getType(), val)))
                .collect(Collectors.toList());
    }

    /**
     * Gets the assignees of a usage node
     * @param assignments Resolved assignment container
     * @return List of resolved assignees matching the node
     */
    private List<AbstractAssignee> getUsageNodeAssignments(Entity node, Assignments assignments) {
        UsageScenario usageScenario = PCMQueryUtils.findParentOfType(node, UsageScenario.class, false)
                .orElseThrow(IllegalStateException::new);
        return assignments.getAssignee()
                .stream()
                .filter(UsageAssignee.class::isInstance)
                .map(UsageAssignee.class::cast)
                .filter(it -> it.getUsagescenario()
                        .equals(usageScenario))
                .collect(Collectors.toList());
    }

    /**
     * Gets the assignees of a SEFF node
     * @param assignments Resolved assignment container
     * @param context Context of the SEFF node
     * @return List of resolved assignees matching the node
     */
    private List<AbstractAssignee> getSEFFNodeAssignments(Assignments assignments, Deque<AssemblyContext> context) {
        List<AbstractAssignee> resolvedAssignees = new ArrayList<>();
        var allocations = this.resourceLoader.lookupToplevelElement(AllocationPackage.eINSTANCE.getAllocation())
                .stream()
                .filter(Allocation.class::isInstance)
                .map(Allocation.class::cast)
                .toList();

        Optional<Allocation> allocation = allocations.stream()
                .filter(it -> it.getAllocationContexts_Allocation()
                        .stream()
                        .map(AllocationContext::getAssemblyContext_AllocationContext)
                        .anyMatch(context.getFirst()::equals))
                .findFirst();

        if (allocation.isEmpty()) {
            logger.error("Could not find fitting allocation for assembly context of SEFF Node");
            throw new IllegalStateException();
        }

        var allocationContexts = allocation.get()
                .getAllocationContexts_Allocation();
        allocationContexts.stream()
                .filter(it -> context.contains(it.getAssemblyContext_AllocationContext()))
                .forEach(it -> {
                    resolvedAssignees.addAll(getAllocationAssignees(assignments, it.getAssemblyContext_AllocationContext()));
                    resolvedAssignees.addAll(getResourceAssignees(assignments, it.getResourceContainer_AllocationContext()));
                    if (it.getAssemblyContext_AllocationContext()
                            .getEncapsulatedComponent__AssemblyContext() instanceof CompositeComponent) {
                        resolvedAssignees.addAll(
                                getCompositeComponentAssignees(assignments, context, (CompositeComponent) it.getAssemblyContext_AllocationContext()
                                        .getEncapsulatedComponent__AssemblyContext()));
                    }
                });
        return resolvedAssignees;
    }

    /**
     * Gets the assembly assignees of a aSEFF node
     * @param assignments Resolved assignment container
     * @param assemblyContext Given assembly context
     * @return List of resolved assignees matching the node
     */
    private List<AbstractAssignee> getAllocationAssignees(Assignments assignments, AssemblyContext assemblyContext) {
        return assignments.getAssignee()
                .stream()
                .filter(AssemblyAssignee.class::isInstance)
                .map(AssemblyAssignee.class::cast)
                .filter(it -> it.getAssemblycontext()
                        .equals(assemblyContext))
                .collect(Collectors.toList());
    }

    /**
     * Gets the resource assignees of a SEFF node
     * @param assignments Resolved assignment container
     * @param resourceContainer Given resource container
     * @return List of resolved assignees matching the node
     */
    private List<AbstractAssignee> getResourceAssignees(Assignments assignments, ResourceContainer resourceContainer) {
        return assignments.getAssignee()
                .stream()
                .filter(ResourceAssignee.class::isInstance)
                .map(ResourceAssignee.class::cast)
                .filter(it -> it.getResourcecontainer()
                        .equals(resourceContainer))
                .collect(Collectors.toList());
    }

    /**
     * Gets the contained assembly assignees of a SEFF node in a composite component
     * @param assignments Resolved assignment container
     * @param context Context of the node
     * @param compositeComponent Given composite component
     * @return Returns the list of all contained assignees of a SEFF node inside a given composite component
     */
    private List<AbstractAssignee> getCompositeComponentAssignees(Assignments assignments, Deque<AssemblyContext> context,
            CompositeComponent compositeComponent) {
        List<AbstractAssignee> evaluatedAssignees = new ArrayList<>();
        List<AssemblyContext> assemblyContexts = compositeComponent.getAssemblyContexts__ComposedStructure();
        for (AssemblyContext assemblyContext : assemblyContexts) {
            if (context.contains(assemblyContext)) {
                evaluatedAssignees.addAll(this.getAllocationAssignees(assignments, assemblyContext));
            }
        }
        return evaluatedAssignees;
    }

    /**
     * Resolves the assignment container in the list of loaded resources
     * @return Assignment container of the model, or a dummy container, if no assignments exist
     */
    private Assignments resolveAssignments() {
        return this.resourceLoader.lookupToplevelElement(NodeCharacteristicsPackage.eINSTANCE.getAssignments())
                .stream()
                .filter(Assignments.class::isInstance)
                .map(Assignments.class::cast)
                .findFirst()
                .orElse(NodeCharacteristicsFactory.eINSTANCE.createAssignments());
    }

    public void checkAssignments() {
        Assignments assignments = this.resolveAssignments();
        for (AbstractAssignee assignee : assignments.getAssignee()) {
            if (assignee instanceof UsageAssignee usage) {
                if (!this.presentInUsageModel(usage.getUsagescenario())) {
                    throw new IllegalStateException("Referenced Usage Scenario is not loaded!");
                }
                this.checkCharacteristics(usage.getCharacteristics());
            } else if (assignee instanceof ResourceAssignee resource) {
                if (!this.presentInResource(resource.getResourcecontainer())) {
                    throw new IllegalStateException("Referenced Resource container is not loaded!");
                }
                this.checkCharacteristics(resource.getCharacteristics());
            } else if (assignee instanceof AssemblyAssignee assembly) {
                if (!this.presentInAssembly(assembly.getAssemblycontext()) && !this.presentInComposite(assembly.getAssemblycontext())) {
                    throw new IllegalStateException("Referenced Assembly context is not loaded!");
                }
                this.checkCharacteristics(assembly.getCharacteristics());
            } else {
                throw new IllegalStateException("Assignments contain unknown assignment target");
            }
        }
    }

    /**
     * Determines whether a given usageScenario is currently loaded in the resources of the analysis
     * @param usageScenario Given usage scenario that is searched for
     * @return Returns true, if the usage scenario could be found in the resources of the analysis. Otherwise, the method
     * returns false.
     */
    private boolean presentInUsageModel(UsageScenario usageScenario) {
        List<UsageModel> usageModel = this.resourceLoader.lookupToplevelElement(UsagemodelPackage.eINSTANCE.getUsageModel())
                .parallelStream()
                .filter(UsageModel.class::isInstance)
                .map(UsageModel.class::cast)
                .toList();
        return usageModel.stream()
                .anyMatch(it -> it.getUsageScenario_UsageModel()
                        .contains(usageScenario));
    }

    /**
     * Determines whether a given resource container is currently loaded in the resources of the analysis
     * @param resourceContainer Given resource container that is searched for
     * @return Returns true, if the model object could be found in the resources of the analysis. Otherwise, the method
     * returns false.
     */
    private boolean presentInResource(ResourceContainer resourceContainer) {
        List<ResourceEnvironment> resourceEnvironments = this.resourceLoader
                .lookupToplevelElement(ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment())
                .parallelStream()
                .filter(ResourceEnvironment.class::isInstance)
                .map(ResourceEnvironment.class::cast)
                .toList();
        return resourceEnvironments.stream()
                .anyMatch(it -> it.getResourceContainer_ResourceEnvironment()
                        .contains(resourceContainer));
    }

    /**
     * Determines whether a given assembly context is currently loaded in the resources of the analysis
     * @param assemblyContext Given assembly context that is searched
     * @return Returns true, if the model object could be found in the resources of the analysis. Otherwise, the method
     * returns false.
     */
    private boolean presentInAssembly(AssemblyContext assemblyContext) {
        List<System> systems = this.resourceLoader.lookupToplevelElement(SystemPackage.eINSTANCE.getSystem())
                .parallelStream()
                .filter(System.class::isInstance)
                .map(System.class::cast)
                .toList();
        return systems.stream()
                .anyMatch(it -> it.getAssemblyContexts__ComposedStructure()
                        .contains(assemblyContext));
    }

    /**
     * Determines whether a given assembly context is currently loaded in the resources of the analysis
     * @param assemblyContext Given assembly context that is searched
     * @return Returns true, if the model object could be found in the resources of the analysis. Otherwise, the method
     * returns false.
     */
    private boolean presentInComposite(AssemblyContext assemblyContext) {
        List<Repository> repositories = this.resourceLoader.lookupToplevelElement(RepositoryPackage.eINSTANCE.getRepository())
                .parallelStream()
                .filter(Repository.class::isInstance)
                .map(Repository.class::cast)
                .toList();
        List<CompositeComponent> compositeComponents = repositories.parallelStream()
                .flatMap(it -> it.getComponents__Repository()
                        .stream())
                .filter(CompositeComponent.class::isInstance)
                .map(CompositeComponent.class::cast)
                .toList();
        return compositeComponents.stream()
                .anyMatch(it -> it.getAssemblyContexts__ComposedStructure()
                        .contains(assemblyContext));
    }

    /**
     * Determines, whether the given list of enum characteristics is permissible
     * @param characteristics List of enum characteristics that should be checked
     */
    public void checkCharacteristics(List<EnumCharacteristic> characteristics) {
        for (EnumCharacteristic characteristic : characteristics) {
            List<Literal> allowedLiterals = characteristic.getType()
                    .getType()
                    .getLiterals();
            List<Literal> foundLiterals = characteristic.getValues();
            List<Literal> unknownLiterals = foundLiterals.parallelStream()
                    .filter(it -> !allowedLiterals.contains(it))
                    .toList();
            if (!unknownLiterals.isEmpty()) {
                throw new IllegalStateException("Found unknown literal " + unknownLiterals.get(0)
                        .getName() + " in assigned characteristics!");
            }
        }
    }
}
