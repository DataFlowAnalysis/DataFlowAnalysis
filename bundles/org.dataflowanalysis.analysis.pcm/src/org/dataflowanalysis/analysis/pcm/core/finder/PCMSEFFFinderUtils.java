package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.pcm.utils.SEFFWithContext;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class PCMSEFFFinderUtils {
    private static final Logger logger = Logger.getLogger(PCMSEFFFinderUtils.class);

    public static List<PCMPartialFlowGraph> findSequencesForSEFFAction(AbstractAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {

        switch (currentAction) {
            case StartAction startAction -> {
                return findSequencesForSEFFStartAction(startAction, context, previousSequence, resourceProvider);
            }
            case StopAction stopAction -> {
                return findSequencesForSEFFStopAction(stopAction, context, previousSequence, resourceProvider);
            }
            case ExternalCallAction externalCallAction -> {
                return findSequencesForSEFFExternalCallAction(externalCallAction, context, previousSequence, resourceProvider);
            }
            case SetVariableAction setVariableAction -> {
                return findSequencesForSEFFSetVariableAction(setVariableAction, context, previousSequence, resourceProvider);
            }
            case BranchAction branchAction -> {
                return findSequencesForSEFFBranchAction(branchAction, context, previousSequence, resourceProvider);
            }
            case null, default -> {
                // default case: skip action and continue with successor
                logger.info(
                        String.format("Action %s has unsupported type of %s and is skipped.", currentAction.getId(), currentAction.getClass().getName()));
                return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, previousSequence, resourceProvider);
            }
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFStartAction(StartAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        var startElement = new SEFFPCMVertex<StartAction>(currentAction, List.of(previousSequence.getSink()), context.getContext(),
                context.getParameter(), resourceProvider);
        var currentSequence = new PCMPartialFlowGraph(startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentSequence, resourceProvider);
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFStopAction(StopAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        var stopElement = new SEFFPCMVertex<StopAction>(currentAction, List.of(previousSequence.getSink()), context.getContext(),
                context.getParameter(), resourceProvider);
        var currentSequence = new PCMPartialFlowGraph(stopElement);

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class, false);
        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get().getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, currentSequence, resourceProvider);
        } else {
            AbstractVertex<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return returnToCaller(caller, context, currentSequence, resourceProvider);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {

        var callingEntity = new CallingSEFFPCMVertex(currentAction, List.of(previousSequence.getSink()), context.getContext(), context.getParameter(),
                true, resourceProvider);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context.getContext());

        if (calledSEFF.isEmpty()) {
            return List.of(previousSequence);
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get().seff().getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                context.addCaller(callingEntity);
                context.updateSEFFContext(calledSEFF.get().context());
                context.updateParametersForCall(calledSignature.getParameters__OperationSignature());

                return findSequencesForSEFFAction(SEFFStartAction.get(), context, currentActionSequence, resourceProvider);
            }
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {

        var newEntity = new SEFFPCMVertex<>(currentAction, List.of(previousSequence.getSink()), context.getContext(), context.getParameter(),
                resourceProvider);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentActionSequence, resourceProvider);
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFBranchAction(BranchAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        return currentAction.getBranches_Branch().stream().map(AbstractBranchTransition::getBranchBehaviour_BranchTransition)
                .map(ResourceDemandingBehaviour::getSteps_Behaviour).map(PCMQueryUtils::getFirstStartActionInActionList).flatMap(Optional::stream)
                .map(it -> {
                    Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism = new IdentityHashMap<>();
                    PCMPartialFlowGraph clonedSequence = previousSequence.deepCopy(isomorphism);
                    SEFFFinderContext clonedContext = new SEFFFinderContext(context);
                    clonedContext.replaceCallers(isomorphism);
                    return findSequencesForSEFFAction(it, clonedContext, clonedSequence, resourceProvider);
                }).flatMap(List::stream).toList();
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFActionReturning(ExternalCallAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider, AbstractPCMVertex<?> caller) {
        List<AbstractPCMVertex<?>> previousVertices = new ArrayList<>();
        previousVertices.add(caller);
        previousVertices.add(previousSequence.getSink());
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(
                new CallingSEFFPCMVertex(currentAction, previousVertices, context.getContext(), context.getParameter(), false, resourceProvider));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentActionSequence, resourceProvider);
    }

    public static List<PCMPartialFlowGraph> returnToCaller(AbstractVertex<?> caller, SEFFFinderContext context, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {

        if (caller instanceof CallingUserPCMVertex) {
            return returnToUserCaller((CallingUserPCMVertex) caller, context, previousSequence, resourceProvider);

        } else if (caller instanceof CallingSEFFPCMVertex) {
            return returnToSEFFCaller((CallingSEFFPCMVertex) caller, context, previousSequence, resourceProvider);

        } else {
            throw new IllegalArgumentException(String.format("No dispatch logic for call of type %s available.", caller.getClass().getSimpleName()));
        }
    }

    private static List<PCMPartialFlowGraph> returnToUserCaller(CallingUserPCMVertex caller, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        if (!context.getCallers().isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getReferencedElement(), previousSequence, resourceProvider, caller);
        }
    }

    private static List<PCMPartialFlowGraph> returnToSEFFCaller(CallingSEFFPCMVertex caller, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getReferencedElement(), context, previousSequence, resourceProvider, caller);
    }
}
