package org.dataflowanalysis.analysis.pcm.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.pcm.core.composition.AssemblyConnector;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.composition.ComposedStructure;
import org.palladiosimulator.pcm.core.composition.ProvidedDelegationConnector;
import org.palladiosimulator.pcm.core.composition.RequiredDelegationConnector;
import org.palladiosimulator.pcm.core.entity.InterfaceProvidingEntity;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.ProvidedRole;
import org.palladiosimulator.pcm.repository.RequiredRole;
import org.palladiosimulator.pcm.repository.Signature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;
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
}
