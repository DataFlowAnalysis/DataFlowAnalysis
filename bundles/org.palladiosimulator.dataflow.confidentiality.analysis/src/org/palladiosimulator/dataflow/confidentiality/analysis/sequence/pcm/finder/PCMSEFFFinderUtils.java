package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.OperationRequiredRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.AbstractBranchTransition;
import org.palladiosimulator.pcm.seff.BranchAction;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingBehaviour;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class PCMSEFFFinderUtils {
	public static List<ActionSequence> findSequencesForSEFFAction(AbstractAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {

        if (currentAction instanceof StartAction) {
            return findSequencesForSEFFStartAction((StartAction) currentAction, context, callers, availableVariables, previousSequence);

        } else if (currentAction instanceof StopAction) {
            return findSequencesForSEFFStopAction((StopAction) currentAction, context, callers, availableVariables, previousSequence);

        } else if (currentAction instanceof ExternalCallAction) {
            return findSequencesForSEFFExternalCallAction((ExternalCallAction) currentAction, context, callers, availableVariables, 
                    previousSequence);

        } else if (currentAction instanceof SetVariableAction) {
            return findSequencesForSEFFSetVariableAction((SetVariableAction) currentAction, context, callers, availableVariables, 
                    previousSequence);

        } else if (currentAction instanceof BranchAction) {
            return findSequencesForSEFFBranchAction((BranchAction) currentAction, context, callers, availableVariables, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("The type %s is not supported in SEFFs", currentAction.getClass()
                        .getName()));
        }
    }

    private static List<ActionSequence> findSequencesForSEFFStartAction(StartAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {
    	var startElement = new SEFFActionSequenceElement<StartAction>(currentAction, context, availableVariables);
    	var currentSequence = new ActionSequence(previousSequence, startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers, availableVariables,
                currentSequence);
    }

    private static List<ActionSequence> findSequencesForSEFFStopAction(StopAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, callers, availableVariables, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = callers.removeLast();
            List<Parameter> parentVariables = getParametersCaller(caller);
            return returnToCaller(caller, callers, parentVariables, previousSequence);
        }
    }

    private static List<ActionSequence> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {

        var callingEntity = new CallingSEFFActionSequenceElement(currentAction, context, availableVariables, true);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context);

        if (calledSEFF.isEmpty()) {
            return List.of(previousSequence);
        } else {
        	if (calledSEFF.get().seff().getBasicComponent_ServiceEffectSpecification() instanceof OperationalDataStoreComponent) {
        		callers.add(callingEntity);
                List<Parameter> availableToCallee = calledSignature.getParameters__OperationSignature();
        		return PCMDatabaseFinderUtils.findSequencesForDatabaseAction(calledSEFF.get(), 
        				calledSEFF.get().context(), callers, availableToCallee, currentActionSequence);
        	}
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .seff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                callers.add(callingEntity);
                List<Parameter> availableToCallee = calledSignature.getParameters__OperationSignature();
                return findSequencesForSEFFAction(SEFFStartAction.get(), calledSEFF.get()
                    .context(), callers, availableToCallee, currentActionSequence);
            }
        }

    }

    private static List<ActionSequence> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {

        var newEntity = new SEFFActionSequenceElement<>(currentAction, context, availableVariables);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers, availableVariables,
                currentActionSequence);
    }

    private static List<ActionSequence> findSequencesForSEFFBranchAction(BranchAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {

        return currentAction.getBranches_Branch()
            .stream()
            .map(AbstractBranchTransition::getBranchBehaviour_BranchTransition)
            .map(ResourceDemandingBehaviour::getSteps_Behaviour)
            .map(PCMQueryUtils::getFirstStartActionInActionList)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForSEFFAction(it, context, new ArrayDeque<>(callers), new ArrayList<>(availableVariables), previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private static List<ActionSequence> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables,
            ActionSequence previousSequence) {
        ActionSequence currentActionSequence = new ActionSequence(previousSequence,
                new CallingSEFFActionSequenceElement(currentAction, context, availableVariables, false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers, availableVariables,
                currentActionSequence);
    }

    public static List<ActionSequence> returnToCaller(AbstractPCMActionSequenceElement<?> caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables, ActionSequence previousSequence) {

        if (caller instanceof CallingUserActionSequenceElement) {
            return returnToUserCaller((CallingUserActionSequenceElement) caller, callers, previousSequence);

        } else if (caller instanceof CallingSEFFActionSequenceElement) {
            return returnToSEFFCaller((CallingSEFFActionSequenceElement) caller, callers, availableVariables, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("No dispatch logic for call of type %s available.", caller.getClass()
                        .getSimpleName()));
        }
    }

    private static List<ActionSequence> returnToUserCaller(CallingUserActionSequenceElement caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, ActionSequence previousSequence) {
        if (!callers.isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getElement(), previousSequence);
        }
    }

    private static List<ActionSequence> returnToSEFFCaller(CallingSEFFActionSequenceElement caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, List<Parameter> availableVariables, ActionSequence previousSequence) {
        return findSequencesForSEFFActionReturning(caller.getElement(), caller.getContext(), callers, availableVariables, previousSequence);
    }
    
    // TODO: Duplicate

    
    public static List<Parameter> getParametersCaller(AbstractPCMActionSequenceElement<?> caller) {
    	if (caller instanceof CallingUserActionSequenceElement) {
    		CallingUserActionSequenceElement callingUserElement = (CallingUserActionSequenceElement) caller;
    		return callingUserElement.getElement().getOperationSignature__EntryLevelSystemCall().getParameters__OperationSignature();
    	}
    	return caller.getParameter();
    }
}
