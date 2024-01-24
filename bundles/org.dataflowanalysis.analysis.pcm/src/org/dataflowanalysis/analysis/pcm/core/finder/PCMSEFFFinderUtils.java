package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMActionSequence;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserActionSequenceElement;
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
	
	public static List<PCMActionSequence> findSequencesForSEFFAction(AbstractAction currentAction, SEFFFinderContext context,
            PCMActionSequence previousSequence) {

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

    private static List<PCMActionSequence> findSequencesForSEFFStartAction(StartAction currentAction, SEFFFinderContext context, PCMActionSequence previousSequence) {
    	var startElement = new SEFFActionSequenceElement<StartAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new PCMActionSequence(previousSequence, startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentSequence);
    }

    private static List<PCMActionSequence> findSequencesForSEFFStopAction(StopAction currentAction, SEFFFinderContext context, PCMActionSequence previousSequence) {
    	var stopElement = new SEFFActionSequenceElement<StopAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new PCMActionSequence(previousSequence, stopElement);
    	
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

    private static List<PCMActionSequence> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction, SEFFFinderContext context,
            PCMActionSequence previousSequence) {

        var callingEntity = new CallingSEFFActionSequenceElement(currentAction, context.getContext(), context.getParameter(), true);
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence, callingEntity);

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

    private static List<PCMActionSequence> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            SEFFFinderContext context,
            PCMActionSequence previousSequence) {

        var newEntity = new SEFFActionSequenceElement<>(currentAction, context.getContext(), context.getParameter());
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    private static List<PCMActionSequence> findSequencesForSEFFBranchAction(BranchAction currentAction,
            SEFFFinderContext context,
            PCMActionSequence previousSequence) {

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

    private static List<PCMActionSequence> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            SEFFFinderContext context,
            PCMActionSequence previousSequence) {
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence,
                new CallingSEFFActionSequenceElement(currentAction, context.getContext(), context.getParameter(), false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    public static List<PCMActionSequence> returnToCaller(AbstractPCMVertex<?> caller,
            SEFFFinderContext context, PCMActionSequence previousSequence) {

        if (caller instanceof CallingUserActionSequenceElement) {
            return returnToUserCaller((CallingUserActionSequenceElement) caller, context, previousSequence);

        } else if (caller instanceof CallingSEFFActionSequenceElement) {
            return returnToSEFFCaller((CallingSEFFActionSequenceElement) caller, context, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("No dispatch logic for call of type %s available.", caller.getClass()
                        .getSimpleName()));
        }
    }

    private static List<PCMActionSequence> returnToUserCaller(CallingUserActionSequenceElement caller,
            SEFFFinderContext context, PCMActionSequence previousSequence) {
        if (!context.getCallers().isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getElement(), previousSequence);
        }
    }

    private static List<PCMActionSequence> returnToSEFFCaller(CallingSEFFActionSequenceElement caller,
            SEFFFinderContext context, PCMActionSequence previousSequence) {
    	context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getElement(), context, previousSequence);
    }
}
