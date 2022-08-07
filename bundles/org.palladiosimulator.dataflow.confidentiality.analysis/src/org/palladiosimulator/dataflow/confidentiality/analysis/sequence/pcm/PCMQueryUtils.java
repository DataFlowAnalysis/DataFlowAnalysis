package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

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
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;

public class PCMQueryUtils {

    private static final Logger logger = Logger.getLogger(PCMQueryUtils.class);

    private PCMQueryUtils() {
        // Utility class
    }

    public static Optional<Start> getStartActionOfScenarioBehavior(ScenarioBehaviour scenarioBehavior) {
        List<Start> candidates = scenarioBehavior.getActions_ScenarioBehaviour()
            .stream()
            .filter(it -> it instanceof Start)
            .map(it -> (Start) it)
            .toList();

        if (candidates.size() > 1) {
            logger.warn(String.format("UsageScenario %s contains more than one start action.",
                    scenarioBehavior.getEntityName()));
        }

        return candidates.stream()
            .findFirst();
    }

    public static Optional<StartAction> getFirstStartActionInActionList(List<AbstractAction> actionList) {
        return actionList.stream()
            .filter(it -> it instanceof StartAction)
            .map(it -> (StartAction) it)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> findParentOfType(EObject object, Class<T> clazz, boolean includeSelf) {
        var currentObject = includeSelf ? object : object.eContainer();

        while (currentObject != null && !clazz.isInstance(currentObject)) {
            currentObject = currentObject.eContainer();
        }

        return Optional.ofNullable((T) currentObject);
    }

    /**
     * Finds a called SEFF and the corresponding stack of assembly contexts. It requires the context
     * of the resolution process to be specified as stack of assembly contexts. The resulting stack
     * can be completely different to the stack from which the call originated because composite
     * components do not provide SEFFs but only contribute to the stack.
     * 
     * @param providedRole
     *            The provided role that points to the identifying component.
     * @param calledSignature
     *            The signature that the SEFF describes.
     * @param context
     *            The stack of assembly contexts that identifies the point from which the call shall
     *            be resolved. The list starts with the most outer assembly context.
     * @return A tuple of the resolved SEFF and the assembly context stack.
     */
    public static Optional<SEFFWithContext> findCalledSEFF(ProvidedRole providedRole, Signature calledSignature,
            Deque<AssemblyContext> context) {

        Deque<AssemblyContext> newContexts = new ArrayDeque<>(context);
        ProvidedRole role = providedRole;
        InterfaceProvidingEntity providingComponent = role.getProvidingEntity_ProvidedRole(); // FIXME: This is null / not resolved correctly in the test model

        while (providingComponent instanceof ComposedStructure) {
            Optional<ProvidedDelegationConnector> connector = findProvidedDelegationConnector(
                    (ComposedStructure) providingComponent, role);

            if (connector.isEmpty()) {
                logger.error("Unable to find provided delegation connector.");
                return Optional.empty();
            } else {
                AssemblyContext assemblyContext = connector.get()
                    .getAssemblyContext_ProvidedDelegationConnector();
                newContexts.add(assemblyContext);

                role = connector.get()
                    .getInnerProvidedRole_ProvidedDelegationConnector();
                providingComponent = role.getProvidingEntity_ProvidedRole();
            }
        }

        if (providingComponent instanceof BasicComponent) {
            BasicComponent component = (BasicComponent) providingComponent;

            Optional<ResourceDemandingSEFF> SEFF = component.getServiceEffectSpecifications__BasicComponent()
                .stream()
                .filter(it -> it instanceof ResourceDemandingSEFF)
                .map(it -> (ResourceDemandingSEFF) it)
                .filter(it -> it.getDescribedService__SEFF()
                    .equals(calledSignature))
                .findFirst();

            if (SEFF.isEmpty()) {
                logger.error("Unable to find called seff.");
                return Optional.empty();
            } else {
                return Optional.of(new SEFFWithContext(SEFF.get(), newContexts));
            }

        } else {
            logger.warn("Unable to find called seff.");
            return Optional.empty();
        }
    }

    /**
     * Finds a called SEFF and the corresponding stack of assembly contexts. It requires the context
     * of the resolution process to be specified as stack of assembly contexts. The resulting stack
     * can be completely different to the stack from which the call originated because composite
     * components do not provide SEFFs but only contribute to the stack.
     * 
     * @param requiredRole
     *            The required role that points to the required component.
     * @param calledSignature
     *            The signature that the SEFF describes.
     * @param context
     *            The stack of assembly contexts that identifies the point from which the call shall
     *            be resolved. The list starts with the most outer assembly context.
     * @return A tuple of the resolved SEFF and the assembly context stack.
     */
    public static Optional<SEFFWithContext> findCalledSEFF(RequiredRole requiredRole,
            OperationSignature calledSignature, Deque<AssemblyContext> context) {

        ComposedStructure composedStructure = context.getLast()
            .getParentStructure__AssemblyContext();
        Deque<AssemblyContext> newContexts = new ArrayDeque<>(context);

        // test if there is an assembly connector satisfying the required role
        Optional<AssemblyConnector> assemblyConnector = composedStructure.getConnectors__ComposedStructure()
            .stream()
            .filter(it -> it instanceof AssemblyConnector)
            .map(it -> (AssemblyConnector) it)
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
                .filter(it -> it instanceof RequiredDelegationConnector)
                .map(it -> (RequiredDelegationConnector) it)
                .filter(it -> it.getInnerRequiredRole_RequiredDelegationConnector()
                    .equals(requiredRole))
                .map(it -> it.getOuterRequiredRole_RequiredDelegationConnector())
                .map(it -> (RequiredRole) it)
                .findFirst();

            if (outerRequiredRole.isEmpty()) {
                logger.error("Unable to retrieve outer required role.");
                return Optional.empty();
            } else {
                newContexts.remove(newContexts.getLast());
                return findCalledSEFF(outerRequiredRole.get(), calledSignature, newContexts);
            }
        }
    }

    private static Optional<ProvidedDelegationConnector> findProvidedDelegationConnector(ComposedStructure component,
            ProvidedRole outerRole) {
        return component.getConnectors__ComposedStructure()
            .stream()
            .filter(it -> it instanceof ProvidedDelegationConnector)
            .map(it -> (ProvidedDelegationConnector) it)
            .filter(it -> it.getOuterProvidedRole_ProvidedDelegationConnector()
                .equals(outerRole))
            .findFirst();
    }

}
