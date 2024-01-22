package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder;

import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.AbstractPCMVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMFlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.CallingSEFFVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff.SEFFVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.user.CallingUserVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.SEFFWithContext;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMQueryUtils;
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
	public static List<PCMFlowGraph> findSequencesForSEFFAction(AbstractAction currentAction, SEFFFinderContext context,
            PCMFlowGraph previousSequence) {

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

    private static List<PCMFlowGraph> findSequencesForSEFFStartAction(StartAction currentAction, SEFFFinderContext context, PCMFlowGraph previousSequence) {
    	var startElement = new SEFFVertex<StartAction>(currentAction, context.getContext(), context.getParameter());
        var currentSequence = new PCMFlowGraph(previousSequence, startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, currentSequence);
    }

    private static List<PCMFlowGraph> findSequencesForSEFFStopAction(StopAction currentAction, SEFFFinderContext context, PCMFlowGraph previousSequence) {

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, previousSequence);
        } else {
            AbstractPCMVertex<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return returnToCaller(caller, context, previousSequence);
        }
    }

    private static List<PCMFlowGraph> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction, SEFFFinderContext context,
            PCMFlowGraph previousSequence) {

        var callingEntity = new CallingSEFFVertex(currentAction, context.getContext(), context.getParameter(), true);
        PCMFlowGraph currentActionSequence = new PCMFlowGraph(previousSequence, callingEntity);

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

    private static List<PCMFlowGraph> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            SEFFFinderContext context,
            PCMFlowGraph previousSequence) {

        var newEntity = new SEFFVertex<>(currentAction, context.getContext(), context.getParameter());
        PCMFlowGraph currentActionSequence = new PCMFlowGraph(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    private static List<PCMFlowGraph> findSequencesForSEFFBranchAction(BranchAction currentAction,
            SEFFFinderContext context,
            PCMFlowGraph previousSequence) {

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

    private static List<PCMFlowGraph> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            SEFFFinderContext context,
            PCMFlowGraph previousSequence) {
        PCMFlowGraph currentActionSequence = new PCMFlowGraph(previousSequence,
                new CallingSEFFVertex(currentAction, context.getContext(), context.getParameter(), false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context,
                currentActionSequence);
    }

    public static List<PCMFlowGraph> returnToCaller(AbstractPCMVertex<?> caller,
            SEFFFinderContext context, PCMFlowGraph previousSequence) {

        if (caller instanceof CallingUserVertex) {
            return returnToUserCaller((CallingUserVertex) caller, context, previousSequence);

        } else if (caller instanceof CallingSEFFVertex) {
            return returnToSEFFCaller((CallingSEFFVertex) caller, context, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("No dispatch logic for call of type %s available.", caller.getClass()
                        .getSimpleName()));
        }
    }

    private static List<PCMFlowGraph> returnToUserCaller(CallingUserVertex caller,
            SEFFFinderContext context, PCMFlowGraph previousSequence) {
        if (!context.getCallers().isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return PCMUserFinderUtils.findSequencesForUserActionReturning(caller.getElement(), context.getDataStores(), previousSequence);
        }
    }

    private static List<PCMFlowGraph> returnToSEFFCaller(CallingSEFFVertex caller,
            SEFFFinderContext context, PCMFlowGraph previousSequence) {
    	context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getElement(), context, previousSequence);
    }
}
