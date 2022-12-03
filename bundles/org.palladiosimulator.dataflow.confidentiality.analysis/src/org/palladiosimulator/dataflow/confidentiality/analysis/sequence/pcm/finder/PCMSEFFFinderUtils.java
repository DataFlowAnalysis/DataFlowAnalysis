package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.SEFFWithContext;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
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
	public static List<ActionSequence> findSequencesForSEFFAction(AbstractAction currentAction, SEFFFinderContext context,
            ActionSequence previousSequence) {

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
            throw new IllegalArgumentException(
                    String.format("The type %s is not supported in SEFFs", currentAction.getClass()
                        .getName()));
        }
    }

    private static List<ActionSequence> findSequencesForSEFFStartAction(StartAction currentAction, SEFFFinderContext context, ActionSequence previousSequence) {
    	var startElement = new SEFFActionSequenceElement<StartAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new ActionSequence(previousSequence, startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentSequence);
    }

    private static List<ActionSequence> findSequencesForSEFFStopAction(StopAction currentAction, SEFFFinderContext context, ActionSequence previousSequence) {

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return returnToCaller(caller, context, previousSequence);
        }
    }

    private static List<ActionSequence> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction, SEFFFinderContext context,
            ActionSequence previousSequence) {

        var callingEntity = new CallingSEFFActionSequenceElement(currentAction, context.getContext(), context.getParameter(), true);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context.getContext());

        if (calledSEFF.isEmpty()) {
            return List.of(previousSequence);
        } else {
        	if (calledSEFF.get().seff().getBasicComponent_ServiceEffectSpecification() instanceof OperationalDataStoreComponent) {
        		context.addCaller(callingEntity);
        		context.updateParametersForCall(calledSignature.getParameters__OperationSignature());
        		context.updateSEFFContext(calledSEFF.get().context());
        		return PCMDatabaseFinderUtils.findSequencesForDatabaseAction(calledSEFF.get(), 
        				context, currentActionSequence);
        	}
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

    private static List<ActionSequence> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            SEFFFinderContext context,
            ActionSequence previousSequence) {

        var newEntity = new SEFFActionSequenceElement<>(currentAction, context.getContext(), context.getParameter());
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    private static List<ActionSequence> findSequencesForSEFFBranchAction(BranchAction currentAction,
            SEFFFinderContext context,
            ActionSequence previousSequence) {

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

    private static List<ActionSequence> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            SEFFFinderContext context,
            ActionSequence previousSequence) {
        ActionSequence currentActionSequence = new ActionSequence(previousSequence,
                new CallingSEFFActionSequenceElement(currentAction, context.getContext(), context.getParameter(), false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    public static List<ActionSequence> returnToCaller(AbstractPCMActionSequenceElement<?> caller,
            SEFFFinderContext context, ActionSequence previousSequence) {

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

    private static List<ActionSequence> returnToUserCaller(CallingUserActionSequenceElement caller,
            SEFFFinderContext context, ActionSequence previousSequence) {
        if (!context.getCallers().isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getElement(), context.getDataStores(), previousSequence);
        }
    }

    private static List<ActionSequence> returnToSEFFCaller(CallingSEFFActionSequenceElement caller,
            SEFFFinderContext context, ActionSequence previousSequence) {
    	context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getElement(), context, previousSequence);
    }
}
