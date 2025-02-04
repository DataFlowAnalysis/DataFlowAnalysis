package org.dataflowanalysis.analysis.pcm.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMTransposeFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.ComposedStructure;
import org.palladiosimulator.pcm.core.composition.Connector;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity;
import org.palladiosimulator.pcm.core.entity.NamedElement;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationInterface;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.repository.RepositoryPackage;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.repository.Signature;
import org.palladiosimulator.pcm.resourceenvironment.ResourceContainer;
import org.palladiosimulator.pcm.resourceenvironment.ResourceEnvironment;
import org.palladiosimulator.pcm.resourceenvironment.ResourceenvironmentPackage;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
import org.palladiosimulator.pcm.system.System;
import org.palladiosimulator.pcm.system.SystemPackage;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class PCMQueryUtils {
    private static final Logger logger = Logger.getLogger(PCMQueryUtils.class);

    private PCMQueryUtils() {
        throw new IllegalStateException("Utility classes should not be instantiated");
    }

    /**
     * Finds the start action of a given scenario behaviour
     * @param scenarioBehavior Given scenario behaviour
     * @return Returns, if it exists, the single start action of the scenario behaviour
     */
    public static Optional<Start> getStartActionOfScenarioBehavior(ScenarioBehaviour scenarioBehavior) {
        logger.info("Finding start action of " + scenarioBehavior.getEntityName());
        List<Start> candidates = scenarioBehavior.getActions_ScenarioBehaviour()
                .stream()
                .filter(Start.class::isInstance)
                .map(Start.class::cast)
                .toList();

        if (candidates.size() > 1) {
            logger.warn(String.format("UsageScenario %s contains more than one start action.", scenarioBehavior.getEntityName()));
        }

        return candidates.stream()
                .findFirst();
    }

    /**
     * Returns the first start action in the list of actions
     * @param actionList Given list of actions
     * @return Returns the first found start action
     */
    public static Optional<StartAction> getFirstStartActionInActionList(List<AbstractAction> actionList) {
        return actionList.stream()
                .filter(StartAction.class::isInstance)
                .map(StartAction.class::cast)
                .findFirst();
    }

    /**
     * Returns the first stop action in the list of actions
     * @param actionList Given list of actions
     * @return Returns the first found stop action
     */
    public static Optional<StopAction> getFirstStopActionInActionList(List<AbstractAction> actionList) {
        return actionList.stream()
                .filter(StopAction.class::isInstance)
                .map(StopAction.class::cast)
                .findFirst();
    }

    /**
     * Returns the List of start actions for a usage model
     * @param usageModel Given usage model
     * @return List of start actions that are provided by the usage model
     */
    public static List<Start> findStartActionsForUsageModel(UsageModel usageModel) {
        return usageModel.getUsageScenario_UsageModel()
                .stream()
                .map(UsageScenario::getScenarioBehaviour_UsageScenario)
                .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
                .flatMap(Optional::stream)
                .toList();
    }

    /**
     * Finds the parent of a given type starting at the given modeling object
     * @param <T> Type of the parent
     * @param object Modeling object the search should be started at
     * @param clazz Type class of the parent
     * @param includeSelf Should be true, if the search should include the container itself. Otherwise, this should be set
     * to false
     * @return Returns, if found, the parent of the given object with the given type
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> findParentOfType(EObject object, Class<T> clazz, boolean includeSelf) {
        var currentObject = includeSelf ? object : object.eContainer();

        while (currentObject != null && !clazz.isInstance(currentObject)) {
            currentObject = currentObject.eContainer();
        }

        return Optional.ofNullable((T) currentObject);
    }

    /**
     * Finds a called SEFF and the corresponding stack of assembly contexts. It requires the context of the resolution
     * process to be specified as stack of assembly contexts. The resulting stack can be completely different to the stack
     * from which the call originated because composite components do not provide SEFFs but only contribute to the stack.
     * @param providedRole The provided role that points to the identifying component.
     * @param calledSignature The signature that the SEFF describes.
     * @param context The stack of assembly contexts that identifies the point from which the call shall be resolved. The
     * list starts with the most outer assembly context.
     * @return A tuple of the resolved SEFF and the assembly context stack.
     */
    public static Optional<SEFFWithContext> findCalledSEFF(ProvidedRole providedRole, Signature calledSignature, Deque<AssemblyContext> context) {

        Deque<AssemblyContext> newContexts = new ArrayDeque<>(context);
        ProvidedRole role = providedRole;
        InterfaceProvidingEntity providingComponent = role.getProvidingEntity_ProvidedRole();

        while (providingComponent instanceof ComposedStructure) {
            Optional<ProvidedDelegationConnector> connector = findProvidedDelegationConnector((ComposedStructure) providingComponent, role);

            if (connector.isEmpty()) {
                throw new IllegalStateException("Unable to find provided delegation connector.");
            } else {
                AssemblyContext assemblyContext = connector.get()
                        .getAssemblyContext_ProvidedDelegationConnector();
                newContexts.add(assemblyContext);

                role = connector.get()
                        .getInnerProvidedRole_ProvidedDelegationConnector();
                providingComponent = role.getProvidingEntity_ProvidedRole();
            }
        }

        if (providingComponent instanceof BasicComponent component) {

            Optional<ResourceDemandingSEFF> SEFF = component.getServiceEffectSpecifications__BasicComponent()
                    .stream()
                    .filter(ResourceDemandingSEFF.class::isInstance)
                    .map(ResourceDemandingSEFF.class::cast)
                    .filter(it -> it.getDescribedService__SEFF()
                            .equals(calledSignature))
                    .findFirst();

            if (SEFF.isEmpty()) {
                throw new IllegalStateException("Unable to find called seff.");
            } else {
                return Optional.of(new SEFFWithContext(SEFF.get(), newContexts));
            }

        } else {
            throw new IllegalStateException("Unable to find called seff.");
        }
    }

    /**
     * Finds a called SEFF and the corresponding stack of assembly contexts. It requires the context of the resolution
     * process to be specified as stack of assembly contexts. The resulting stack can be completely different to the stack
     * from which the call originated because composite components do not provide SEFFs but only contribute to the stack.
     * @param requiredRole The required role that points to the required component.
     * @param calledSignature The signature that the SEFF describes.
     * @param context The stack of assembly contexts that identifies the point from which the call shall be resolved. The
     * list starts with the most outer assembly context.
     * @return A tuple of the resolved SEFF and the assembly context stack.
     */
    public static Optional<SEFFWithContext> findCalledSEFF(RequiredRole requiredRole, OperationSignature calledSignature,
            Deque<AssemblyContext> context) {

        ComposedStructure composedStructure = context.getLast()
                .getParentStructure__AssemblyContext();
        Deque<AssemblyContext> newContexts = new ArrayDeque<>(context);

        // test if there is an assembly connector satisfying the required role
        Optional<AssemblyConnector> assemblyConnector = composedStructure.getConnectors__ComposedStructure()
                .stream()
                .filter(AssemblyConnector.class::isInstance)
                .map(AssemblyConnector.class::cast)
                .filter(it -> it.getRequiredRole_AssemblyConnector()
                        .equals(requiredRole))
                .filter(it -> it.getRequiringAssemblyContext_AssemblyConnector()
                        .equals(newContexts.getLast()))
                .findFirst();

        if (assemblyConnector.isPresent()) {
            newContexts.remove(newContexts.getLast());
            AssemblyContext newAssemblyContext = assemblyConnector.get()
                    .getProvidingAssemblyContext_AssemblyConnector();
            OperationProvidedRole providedRole = assemblyConnector.get()
                    .getProvidedRole_AssemblyConnector();
            newContexts.add(newAssemblyContext);
            return findCalledSEFF(providedRole, calledSignature, newContexts);
        } else {

            // go to the parent composed structure to satisfy the required role
            Optional<RequiredRole> outerRequiredRole = composedStructure.getConnectors__ComposedStructure()
                    .stream()
                    .filter(RequiredDelegationConnector.class::isInstance)
                    .map(RequiredDelegationConnector.class::cast)
                    .filter(it -> it.getInnerRequiredRole_RequiredDelegationConnector()
                            .equals(requiredRole))
                    .map(RequiredDelegationConnector::getOuterRequiredRole_RequiredDelegationConnector)
                    .map(RequiredRole.class::cast)
                    .findFirst();

            if (outerRequiredRole.isEmpty()) {
                throw new IllegalStateException("Unable to retrieve outer required role.");
            } else {
                newContexts.remove(newContexts.getLast());
                return findCalledSEFF(outerRequiredRole.get(), calledSignature, newContexts);
            }
        }
    }

    private static Optional<ProvidedDelegationConnector> findProvidedDelegationConnector(ComposedStructure component, ProvidedRole outerRole) {
        return component.getConnectors__ComposedStructure()
                .stream()
                .filter(ProvidedDelegationConnector.class::isInstance)
                .map(ProvidedDelegationConnector.class::cast)
                .filter(it -> it.getOuterProvidedRole_ProvidedDelegationConnector()
                        .equals(outerRole))
                .findFirst();
    }

    /**
     * Finds an {@link AssemblyContext} with the given ID
     * @param flowGraphs Flow Graphs that are searched
     * @param id Given ID the {@link AssemblyContext} must have
     * @return Returns an Optional containing the {@link AssemblyContext} if one can be found
     */
    public static Optional<AssemblyContext> findAssemblyContext(FlowGraphCollection flowGraphs, String id) {
        List<Deque<AssemblyContext>> contexts = PCMQueryUtils.findAllAssemblyContexts(flowGraphs);
        List<AssemblyContext> allContexts = contexts.stream()
                .flatMap(Collection::stream)
                .toList();
        return allContexts.stream()
                .filter(it -> EcoreUtil.getID(it)
                        .equals(id))
                .findFirst();
    }

    /**
     * Finds an {@link Entity} with the given ID
     * @param flowGraphs Flow Graphs that are searched
     * @param id Given ID the {@link Entity} must have
     * @return Returns an Optional containing the {@link Entity} if one can be found
     */
    public static Optional<? extends Entity> findAction(FlowGraphCollection flowGraphs, String id) {
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var candidates = transposeFlowGraph.getVertices()
                    .stream()
                    .map(AbstractPCMVertex.class::cast)
                    .filter(it -> EcoreUtil.getID(it.getReferencedElement())
                            .equals(id))
                    .toList();

            if (!candidates.isEmpty()) {
                return candidates.stream()
                        .map(AbstractPCMVertex::getReferencedElement)
                        .filter(Objects::nonNull)
                        .findFirst();
            }
        }

        return Optional.empty();
    }

    /**
     * Finds an {@link OperationInterface} with the given ID
     * @param resourceProvider Resource provider that is used to look up the repository model
     * @param id Given ID the {@link OperationInterface} must have
     * @return Returns an Optional containing the {@link OperationInterface} if one can be found
     */
    public static Optional<OperationInterface> findInterface(ResourceProvider resourceProvider, String id) {
        return PCMQueryUtils.lookupRepositoryModel(resourceProvider)
                .getInterfaces__Repository()
                .stream()
                .filter(it -> it.getId()
                        .equals(id))
                .filter(OperationInterface.class::isInstance)
                .map(OperationInterface.class::cast)
                .findFirst();
    }

    /**
     * Finds an {@link OperationSignature} with the given ID
     * @param resourceProvider Resource provider that is used to look up the repository model
     * @param id Given ID the {@link OperationSignature} must have
     * @return Returns an Optional containing the {@link OperationSignature} if one can be found
     */
    public static Optional<OperationSignature> findSignature(ResourceProvider resourceProvider, String id) {
        return PCMQueryUtils.lookupRepositoryModel(resourceProvider)
                .getInterfaces__Repository()
                .stream()
                .filter(OperationInterface.class::isInstance)
                .map(OperationInterface.class::cast)
                .map(OperationInterface::getSignatures__OperationInterface)
                .flatMap(Collection::stream)
                .filter(it -> it.getId()
                        .equals(id))
                .findFirst();

    }

    /**
     * Finds an {@link Connector} with the given ID
     * @param resourceProvider Resource provider that is used to look up the system mdeol
     * @param id Given ID the {@link Connector} must have
     * @return Returns an Optional containing the {@link Connector} if one can be found
     */
    public static Optional<Connector> findConnector(ResourceProvider resourceProvider, String id) {
        return PCMQueryUtils.lookupSystemModel(resourceProvider)
                .getConnectors__ComposedStructure()
                .stream()
                .filter(it -> it.getId()
                        .equals(id))
                .findFirst();
    }

    /**
     * Finds an {@link ResourceContainer} with the given ID
     * @param resourceProvider Resource provider that is used to look up the resource environment model
     * @param id Given ID the {@link ResourceContainer} must have
     * @return Returns an Optional containing the {@link ResourceContainer} if one can be found
     */
    public static Optional<ResourceContainer> findResourceContainer(ResourceProvider resourceProvider, String id) {
        return PCMQueryUtils.lookupResourceEnvironmentModel(resourceProvider)
                .getResourceContainer_ResourceEnvironment()
                .stream()
                .filter(it -> it.getId()
                        .equals(id))
                .findFirst();
    }

    /**
     * Finds an {@link UsageScenario} with the given ID
     * @param resourceProvider Resource provider thaz is used to look up the usage model
     * @param id Given ID the {@link UsageScenario} must have
     * @return Returns an Optional containing the {@link UsageScenario} if one can be found
     */
    public Optional<UsageScenario> findUsageScenario(PCMResourceProvider resourceProvider, String id) {
        return PCMQueryUtils.lookupUsageModel(resourceProvider)
                .getUsageScenario_UsageModel()
                .stream()
                .filter(it -> it.getId()
                        .equals(id))
                .findFirst();
    }

    /**
     * Finds all {@link SEFFPCMVertex} that are start actions in the given {@link AssemblyContext}
     * @param flowGraphs Flow Graphs that are searched
     * @param component {@link AssemblyContext} that must be started by the {@link SEFFPCMVertex} elements
     * @return Returns a list of all {@link SEFFPCMVertex} that begin the {@link AssemblyContext}
     */
    public static List<SEFFPCMVertex<?>> findStartActionsOfAssemblyContext(FlowGraphCollection flowGraphs, AssemblyContext component) {
        List<SEFFPCMVertex<?>> matches = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var candidates = transposeFlowGraph.getVertices()
                    .stream()
                    .map(AbstractPCMVertex.class::cast)
                    .filter(it -> it instanceof SEFFPCMVertex<?>)
                    .filter(it -> it.getReferencedElement() instanceof StartAction)
                    .filter(it -> it.getContext()
                            .contains(component))
                    .map(it -> (SEFFPCMVertex<?>) it)
                    .toList();
            matches.addAll(candidates);
        }
        return matches;
    }

    /**
     * Determines all vertices that have the given {@link Entity} as referenced element
     * @param flowGraphs Flow Graphs that are searched
     * @param action Given {@link Entity} that must be the referenced element of the vertex
     * @return Returns a list of vertices with the given referenced {@link Entity}
     */
    public static List<AbstractPCMVertex<?>> findProcessesWithAction(FlowGraphCollection flowGraphs, Entity action) {
        List<AbstractPCMVertex<?>> matches = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var candidates = transposeFlowGraph.getVertices()
                    .stream()
                    .map(AbstractPCMVertex.class::cast)
                    .filter(it -> it.getReferencedElement()
                            .equals(action))
                    .map(it -> (AbstractPCMVertex<?>) it)
                    .toList();

            matches.addAll(candidates);
        }
        return matches;
    }

    /**
     * Finds all {@link CallingUserPCMVertex} that call using the given {@link OperationInterface}
     * @param flowGraphs Flow Graphs that are searched
     * @param operationInterface {@link OperationInterface} that the {@link CallingUserPCMVertex} must call
     * @return Returns a list of {@link CallingUserPCMVertex} that call using the given {@link OperationInterface}
     */
    public static List<CallingUserPCMVertex> findEntryLevelSystemCallsViaInterface(FlowGraphCollection flowGraphs,
            OperationInterface operationInterface) {
        List<CallingUserPCMVertex> matches = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var entryLevelSystemCalls = transposeFlowGraph.getVertices()
                    .stream()
                    .filter(CallingUserPCMVertex.class::isInstance)
                    .map(CallingUserPCMVertex.class::cast)
                    .toList();
            var entryLevelSystemCallsCandidates = entryLevelSystemCalls.stream()
                    .filter(it -> operationInterface.getSignatures__OperationInterface()
                            .contains(it.getReferencedElement()
                                    .getOperationSignature__EntryLevelSystemCall()))
                    .toList();
            matches.addAll(entryLevelSystemCallsCandidates);
        }
        return matches;
    }

    /**
     * Finds all {@link CallingUserPCMVertex} that call using the given {@link OperationSignature}
     * @param flowGraphs Flow Graphs that are searched
     * @param signature {@link OperationSignature} that the {@link CallingUserPCMVertex} must call
     * @return Returns a list of {@link CallingUserPCMVertex} that call using the given {@link OperationSignature}
     */
    public static List<CallingUserPCMVertex> findEntryLevelSystemCallsViaSignature(FlowGraphCollection flowGraphs, OperationSignature signature) {
        var candidates = findEntryLevelSystemCallsViaInterface(flowGraphs, signature.getInterface__OperationSignature());
        return candidates.stream()
                .filter(it -> it.getReferencedElement()
                        .getOperationSignature__EntryLevelSystemCall()
                        .equals(signature))
                .toList();
    }

    /**
     * Finds all {@link CallingSEFFPCMVertex} that call using the given {@link OperationInterface}
     * @param flowGraphs Flow Graphs that are searched
     * @param operationInterface {@link OperationInterface} that the {@link CallingSEFFPCMVertex} must call
     * @return Returns a list of {@link CallingSEFFPCMVertex} that call using the given {@link OperationInterface}
     */
    public static List<CallingSEFFPCMVertex> findExternalCallsViaInterface(FlowGraphCollection flowGraphs, OperationInterface operationInterface) {
        List<CallingSEFFPCMVertex> matches = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var externalCalls = transposeFlowGraph.getVertices()
                    .stream()
                    .filter(CallingSEFFPCMVertex.class::isInstance)
                    .map(CallingSEFFPCMVertex.class::cast)
                    .toList();
            var externalCallCandidates = externalCalls.stream()
                    .filter(it -> operationInterface.getSignatures__OperationInterface()
                            .contains(it.getReferencedElement()
                                    .getCalledService_ExternalService()))
                    .toList();
            matches.addAll(externalCallCandidates);
        }
        return matches;
    }

    /**
     * Finds all {@link CallingSEFFPCMVertex} that call using the given {@link OperationSignature}
     * @param flowGraphs Flow Graphs that are searched
     * @param signature {@link OperationSignature} that the {@link CallingSEFFPCMVertex} must call
     * @return Returns a list of {@link CallingSEFFPCMVertex} that call using the given {@link OperationSignature}
     */
    public static List<CallingSEFFPCMVertex> findExternalCallsViaSignature(FlowGraphCollection flowGraphs, OperationSignature signature) {
        var candidates = PCMQueryUtils.findExternalCallsViaInterface(flowGraphs, signature.getInterface__OperationSignature());
        return candidates.stream()
                .filter(it -> it.getReferencedElement()
                        .getCalledService_ExternalService()
                        .equals(signature))
                .toList();
    }

    /**
     * Finds all the {@link SEFFPCMVertex} elements that are start actions of SEFFs that implement the given
     * {@link OperationInterface}
     * @param flowGraphs Flow Graphs that are searched
     * @param operationInterface {@link OperationInterface} the {@link SEFFPCMVertex} elements must start
     * @return Returns all {@link SEFFPCMVertex} that are {@link StartAction} elements which start the implementation of the
     * given {@link OperationInterface}
     */
    public static List<SEFFPCMVertex<?>> findStartActionsOfSEFFsThatImplement(FlowGraphCollection flowGraphs, OperationInterface operationInterface) {
        List<SEFFPCMVertex<?>> matches = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var startActions = transposeFlowGraph.getVertices()
                    .stream()
                    .map(AbstractPCMVertex.class::cast)
                    .filter(it -> it instanceof SEFFPCMVertex<?>)
                    .filter(it -> (it.getReferencedElement() instanceof StartAction))
                    .map(it -> (SEFFPCMVertex<?>) it)
                    .toList();

            for (SEFFPCMVertex<?> action : startActions) {
                if (!(action.getReferencedElement()
                        .eContainer() instanceof ResourceDemandingSEFF seff)) {
                    continue;
                }
                if (!(seff.getDescribedService__SEFF() instanceof OperationSignature operationSignature)) {
                    continue;
                }
                if (operationInterface.getSignatures__OperationInterface()
                        .contains(operationSignature)) {
                    matches.add(action);
                }
            }
        }
        return matches;
    }

    /**
     * Finds all the {@link SEFFPCMVertex} elements that are start actions of SEFFs that implement the given
     * {@link OperationSignature}
     * @param flowGraphs Flow Graphs that are searched
     * @param signature {@link OperationSignature} the {@link SEFFPCMVertex} elements must start
     * @return Returns all {@link SEFFPCMVertex} that are {@link StartAction} elements which start the implementation of the
     * given {@link OperationSignature}
     */
    public static List<SEFFPCMVertex<?>> findStartActionsOfSEFFsThatImplement(FlowGraphCollection flowGraphs, OperationSignature signature) {
        var actionsThatImplementInterface = PCMQueryUtils.findStartActionsOfSEFFsThatImplement(flowGraphs,
                signature.getInterface__OperationSignature());
        List<SEFFPCMVertex<?>> matches = new ArrayList<>();
        for (SEFFPCMVertex<?> action : actionsThatImplementInterface) {
            if (action.getReferencedElement()
                    .eContainer() instanceof ResourceDemandingSEFF seff) {
                if (signature.equals(seff.getDescribedService__SEFF())) {
                    matches.add(action);
                }
            }
        }
        return matches;
    }

    /**
     * Returns a list of {@link PCMTransposeFlowGraph} that contain the given {@link AbstractPCMVertex}
     * @param flowGraphs Flow Graphs that are searched
     * @param element {@link AbstractPCMVertex} that must be contained in the {@link PCMTransposeFlowGraph}
     * @return Returns a list of all {@link PCMTransposeFlowGraph} that must contain the {@link AbstractPCMVertex}
     */
    public static List<PCMTransposeFlowGraph> findTransposeFlowGraphsWithElement(FlowGraphCollection flowGraphs, AbstractPCMVertex<?> element) {
        return flowGraphs.getTransposeFlowGraphs()
                .stream()
                .filter(PCMTransposeFlowGraph.class::isInstance)
                .filter(it -> it.getVertices()
                        .contains(element))
                .map(PCMTransposeFlowGraph.class::cast)
                .toList();
    }

    /**
     * Determines a list of all possible {@link AssemblyContext} states that occur in all transpose flow graphs
     * @param flowGraphs Flow Graphs that are searched
     * @return Returns a collection containing all {@link AssemblyContext} states in the transpose flow graphs
     */
    private static List<Deque<AssemblyContext>> findAllAssemblyContexts(FlowGraphCollection flowGraphs) {
        List<Deque<AssemblyContext>> allContexts = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            for (AbstractPCMVertex<?> vertex : transposeFlowGraph.getVertices()
                    .stream()
                    .filter(AbstractPCMVertex.class::isInstance)
                    .map(AbstractPCMVertex.class::cast)
                    .toList()) {
                allContexts.add(vertex.getContext());
            }
        }
        return allContexts;
    }

    /**
     * Finds a list of {@link AbstractPCMVertex} that match the given {@link UsageScenario} or {@link ResourceContainer}
     * @param flowGraphs Flow Graphs that are searched
     * @param actor {@link UsageScenario} or {@link ResourceContainer} the {@link AbstractPCMVertex} must match
     * @return Returns a list of {@link AbstractPCMVertex} that match the given {@link UsageScenario} or
     * {@link ResourceContainer}
     */
    public List<? extends AbstractPCMVertex<?>> findProcessesThatRepresentResourceContainerOrUsageScenario(FlowGraphCollection flowGraphs,
            PCMResourceProvider resourceProvider, Entity actor) {
        if (actor instanceof UsageScenario usageScenario) {
            List<CallingUserPCMVertex> matches = new ArrayList<>();
            for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
                var callingUserActions = transposeFlowGraph.getVertices()
                        .stream()
                        .filter(CallingUserPCMVertex.class::isInstance)
                        .map(CallingUserPCMVertex.class::cast)
                        .toList();
                List<CallingUserPCMVertex> candidates = callingUserActions.stream()
                        .filter(it -> it.getReferencedElement()
                                .getScenarioBehaviour_AbstractUserAction()
                                .getUsageScenario_SenarioBehaviour()
                                .equals(usageScenario))
                        .toList();
                matches.addAll(candidates);
            }
            return matches;
        } else if (actor instanceof ResourceContainer resourceContainer) {
            var allocationModel = PCMQueryUtils.lookupAllocationModel(resourceProvider);
            var contextsDeployedOnResource = allocationModel.getAllocationContexts_Allocation()
                    .stream()
                    .filter(it -> it.getResourceContainer_AllocationContext()
                            .equals(resourceContainer))
                    .map(AllocationContext::getAssemblyContext_AllocationContext)
                    .toList();
            List<SEFFPCMVertex<?>> matches = new ArrayList<>();
            for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
                var candidates = transposeFlowGraph.getVertices()
                        .stream()
                        .filter(SEFFPCMVertex.class::isInstance)
                        .map(it -> (SEFFPCMVertex<?>) it)
                        .filter(it -> it.getContext()
                                .stream()
                                .anyMatch(contextsDeployedOnResource::contains))
                        .toList();
                matches.addAll(candidates);
            }
            return matches;
        } else {
            throw new IllegalArgumentException("Actor must be an usage scenario or a resource container.");
        }
    }

    /**
     * Looks up a given PCM model with the required {@link EClass} and of type {@link T}
     * @param resourceProvider Resource provider that is used to look up the PCM model
     * @param eclazz {@link EClass} of the PCM model
     * @param clazz Class of the PCM model element
     * @return Returns a PCM model element of the given class
     * @throws IllegalStateException Thrown if the specified model cannot be found
     * @param <T> Type parameter that describes the class of the returned element
     */
    private static <T extends NamedElement> T lookupPCMModel(ResourceProvider resourceProvider, EClass eclazz, Class<T> clazz) {
        Objects.requireNonNull(eclazz);
        Objects.requireNonNull(clazz);
        List<T> allPCMModelsOfGivenType = resourceProvider.lookupToplevelElement(eclazz)
                .stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
        if (!allPCMModelsOfGivenType.isEmpty()) {
            return allPCMModelsOfGivenType.get(0);
        } else {
            throw new IllegalStateException(
                    String.format("None or more than one model of type %s found in the loaded resources.", clazz.getSimpleName()));
        }
    }

    /**
     * Returns the {@link Repository} model of the contained elements
     * @param resourceProvider Resource provider that is used to look up the {@link Repository} model
     * @return Returns the {@link Repository} model of the contained elements
     */
    private static Repository lookupRepositoryModel(ResourceProvider resourceProvider) {
        return PCMQueryUtils.lookupPCMModel(resourceProvider, RepositoryPackage.eINSTANCE.getRepository(), Repository.class);
    }

    /**
     * Returns the {@link org.palladiosimulator.pcm.system.System} model of the contained elements
     * @param resourceProvider Resource provider that is used to look up the {@link System} model
     * @return Returns the {@link org.palladiosimulator.pcm.system.System} model of the contained elements
     */
    private static System lookupSystemModel(ResourceProvider resourceProvider) {
        return PCMQueryUtils.lookupPCMModel(resourceProvider, SystemPackage.eINSTANCE.getSystem(), System.class);
    }

    /**
     * Returns the {@link ResourceEnvironment} model of the contained elements
     * @param resourceProvider Resource provider that is used to look up the {@link ResourceEnvironment} model
     * @return Returns the {@link ResourceEnvironment} model of the contained elements
     */
    private static ResourceEnvironment lookupResourceEnvironmentModel(ResourceProvider resourceProvider) {
        return PCMQueryUtils.lookupPCMModel(resourceProvider, ResourceenvironmentPackage.eINSTANCE.getResourceEnvironment(),
                ResourceEnvironment.class);
    }

    /**
     * Returns the {@link Allocation} model of the contained elements
     * @param resourceProvider Resource provider that is used to look up the {@link Allocation} model
     * @return Returns the {@link Allocation} model of the contained elements
     */
    private static Allocation lookupAllocationModel(PCMResourceProvider resourceProvider) {
        return resourceProvider.getAllocation();
    }

    /**
     * Returns the {@link UsageModel} of the contained elements
     * @param resourceProvider Resource provider that is used to look up the {@link UsageModel} model
     * @return Returns the {@link UsageModel} of the contained elements
     */
    private static UsageModel lookupUsageModel(PCMResourceProvider resourceProvider) {
        return resourceProvider.getUsageModel();
    }

    /**
     * Finds a list of {@link StartAction} elements of an {@link BranchAction} with the given ID
     * @param flowGraphs Flow Graphs that are searched
     * @param id ID of the {@link BranchAction}
     * @return Returns a list of {@link StartAction} elements with a parent {@link BranchAction} with the required ID
     */
    public static List<StartAction> findStartActionsOfBranchAction(FlowGraphCollection flowGraphs, String id) {
        List<StartAction> matches = new ArrayList<>();

        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            var startActionElements = transposeFlowGraph.getVertices()
                    .stream()
                    .map(AbstractPCMVertex.class::cast)
                    .filter(it -> it instanceof SEFFPCMVertex<?>)
                    .filter(it -> it.getReferencedElement() instanceof StartAction)
                    .toList();

            for (var vertex : startActionElements) {
                Optional<BranchAction> branchAction = PCMQueryUtils.findParentOfType(vertex.getReferencedElement(), BranchAction.class, false);

                if (branchAction.isPresent() && branchAction.get()
                        .getId()
                        .equals(id)) {
                    matches.add((StartAction) vertex.getReferencedElement());
                }
            }
        }

        return matches;
    }
}
