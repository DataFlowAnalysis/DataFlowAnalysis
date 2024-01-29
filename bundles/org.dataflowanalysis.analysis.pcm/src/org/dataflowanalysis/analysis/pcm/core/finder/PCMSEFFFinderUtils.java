package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.pcm.utils.SEFFWithContext;
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
            PCMPartialFlowGraph previousSequence) {

        if (currentAction instanceof StartAction) {
            return findSequencesForSEFFStartAction((StartAction) currentAction, context, previousSequence);

        } else if (currentAction instanceof StopAction) {
            return findSequencesForSEFFStopAction((StopAction) currentAction, context, previousSequence);

        } else if (currentAction instanceof ExternalCallAction) {
            return findSequencesForSEFFExternalCallAction((ExternalCallAction) currentAction, context, previousSequence);

        } else if (currentAction instanceof SetVariableAction) {
            return findSequencesForSEFFSetVariableAction((SetVariableAction) currentAction, context, previousSequence);

        } else if (currentAction instanceof BranchAction) {
            return findSequencesForSEFFBranchAction((BranchAction) currentAction, context, previousSequence);

        } else {
        	// default case: skip action and continue with successor
        	logger.info(String.format("Action %s has unsupported type of %s and is skipped.", 
        			currentAction.getId(), currentAction.getClass().getName()));
        	return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, previousSequence);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFStartAction(StartAction currentAction, SEFFFinderContext context, PCMPartialFlowGraph previousSequence) {
    	var startElement = new SEFFPCMVertex<StartAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new PCMPartialFlowGraph(previousSequence, startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentSequence);
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFStopAction(StopAction currentAction, SEFFFinderContext context, PCMPartialFlowGraph previousSequence) {
    	var stopElement = new SEFFPCMVertex<StopAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new PCMPartialFlowGraph(previousSequence, stopElement);
    	
        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class, false);
        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, currentSequence);
        } else {
            AbstractPCMVertex<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return returnToCaller(caller, context, currentSequence);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction, SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence) {

        var callingEntity = new CallingSEFFPCMVertex(currentAction, context.getContext(), context.getParameter(), true);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(previousSequence, callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context.getContext());

        if (calledSEFF.isEmpty()) {
            return List.of(previousSequence);
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .seff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
            	context.addCaller(callingEntity);
            	context.updateSEFFContext(calledSEFF.get().context());
            	context.updateParametersForCall(calledSignature.getParameters__OperationSignature());
            	
                return findSequencesForSEFFAction(SEFFStartAction.get(), context, currentActionSequence);
            }
        }

    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence) {

        var newEntity = new SEFFPCMVertex<>(currentAction, context.getContext(), context.getParameter());
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFBranchAction(BranchAction currentAction,
            SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence) {

        return currentAction.getBranches_Branch()
            .stream()
            .map(AbstractBranchTransition::getBranchBehaviour_BranchTransition)
            .map(ResourceDemandingBehaviour::getSteps_Behaviour)
            .map(PCMQueryUtils::getFirstStartActionInActionList)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForSEFFAction(it, new SEFFFinderContext(context), previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private static List<PCMPartialFlowGraph> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            SEFFFinderContext context,
            PCMPartialFlowGraph previousSequence) {
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(previousSequence,
                new CallingSEFFPCMVertex(currentAction, context.getContext(), context.getParameter(), false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    public static List<PCMPartialFlowGraph> returnToCaller(AbstractPCMVertex<?> caller,
            SEFFFinderContext context, PCMPartialFlowGraph previousSequence) {

        if (caller instanceof CallingUserPCMVertex) {
            return returnToUserCaller((CallingUserPCMVertex) caller, context, previousSequence);

        } else if (caller instanceof CallingSEFFPCMVertex) {
            return returnToSEFFCaller((CallingSEFFPCMVertex) caller, context, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("No dispatch logic for call of type %s available.", caller.getClass()
                        .getSimpleName()));
        }
    }

    private static List<PCMPartialFlowGraph> returnToUserCaller(CallingUserPCMVertex caller,
            SEFFFinderContext context, PCMPartialFlowGraph previousSequence) {
        if (!context.getCallers().isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getReferencedElement(), previousSequence);
        }
    }

    private static List<PCMPartialFlowGraph> returnToSEFFCaller(CallingSEFFPCMVertex caller,
            SEFFFinderContext context, PCMPartialFlowGraph previousSequence) {
    	context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getReferencedElement(), context, previousSequence);
    }
}
