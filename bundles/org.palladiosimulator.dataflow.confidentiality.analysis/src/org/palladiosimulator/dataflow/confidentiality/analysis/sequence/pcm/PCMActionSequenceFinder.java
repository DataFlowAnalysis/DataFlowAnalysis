package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
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
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class PCMActionSequenceFinder implements ActionSequenceFinder {

    private final UsageModel usageModel;
    private final Allocation allocationModel;

    private final Logger logger = Logger.getLogger(PCMActionSequenceFinder.class);

    public PCMActionSequenceFinder(UsageModel usageModel, Allocation allocationModel) {
        this.usageModel = usageModel;
        this.allocationModel = allocationModel;
    }

    @Override
    public List<ActionSequence> findAllSequences() {
        List<ActionSequence> sequences = findSequencesForUsageModel(usageModel);
        logger.info(String.format("Found %d action %s.", sequences.size(),
                sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<ActionSequence> findSequencesForUsageModel(UsageModel usageModel) {
        ActionSequence initialList = new ActionSequence();
        List<Start> startActions = findStartActionsForUsageModel(usageModel);

        return startActions.stream()
            .map(it -> findSequencesForUserAction(it, initialList))
            .flatMap(List::stream)
            .toList();
    }

    private List<Start> findStartActionsForUsageModel(UsageModel usageModel) {
        return usageModel.getUsageScenario_UsageModel()
            .stream()
            .map(UsageScenario::getScenarioBehaviour_UsageScenario)
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .toList();
    }

    private List<ActionSequence> findSequencesForUserAction(AbstractUserAction currentAction,
            ActionSequence previousSequence) {
        if (currentAction instanceof Start) {
            return findSequencesForUserStartAction((Start) currentAction, previousSequence);

        } else if (currentAction instanceof Stop) {
            return findSequencesForUserStopAction((Stop) currentAction, previousSequence);

        } else if (currentAction instanceof Branch) {
            return findSequencesForUserBranchAction((Branch) currentAction, previousSequence);

        } else if (currentAction instanceof EntryLevelSystemCall) {
            return findSequencesForEntryLevelSystemCall((EntryLevelSystemCall) currentAction, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("The type %s is not supported in usage scenarios.", currentAction.getClass()
                        .getName()));
        }
    }

    private List<ActionSequence> findSequencesForUserStartAction(Start currentAction, ActionSequence previousSequence) {
        return findSequencesForUserAction(currentAction.getSuccessor(), previousSequence);
    }

    private List<ActionSequence> findSequencesForUserStopAction(Stop currentAction, ActionSequence previousSequence) {
        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction,
                AbstractUserAction.class, false);

        if (parentAction.isEmpty()) {
            return List.of(previousSequence);
        } else {
            return findSequencesForUserAction(parentAction.get()
                .getSuccessor(), previousSequence);
        }
    }

    private List<ActionSequence> findSequencesForUserBranchAction(Branch currentAction,
            ActionSequence previousSequence) {
        return currentAction.getBranchTransitions_Branch()
            .stream()
            .map(BranchTransition::getBranchedBehaviour_BranchTransition)
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForUserAction(it, previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private List<ActionSequence> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction,
            ActionSequence previousSequence) {
        var callingEntity = new CallingUserActionSequenceElement(currentAction, true);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature,
                new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<ActionSequence>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .getSeff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMActionSequenceElement<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                return findSequencesForSEFFAction(SEFFStartAction.get(), calledSEFF.get()
                    .getContext(), callers, currentActionSequence);
            }
        }
    }

    private List<ActionSequence> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction,
            ActionSequence previousSequence) {
        ActionSequence currentActionSequence = new ActionSequence(previousSequence,
                new CallingUserActionSequenceElement(currentAction, false));
        return findSequencesForUserAction(currentAction.getSuccessor(), currentActionSequence);
    }

    private List<ActionSequence> findSequencesForSEFFAction(AbstractAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {

        if (currentAction instanceof StartAction) {
            return findSequencesForSEFFStartAction((StartAction) currentAction, context, callers, previousSequence);

        } else if (currentAction instanceof StopAction) {
            return findSequencesForSEFFStopAction((StopAction) currentAction, context, callers, previousSequence);

        } else if (currentAction instanceof ExternalCallAction) {
            return findSequencesForSEFFExternalCallAction((ExternalCallAction) currentAction, context, callers,
                    previousSequence);

        } else if (currentAction instanceof SetVariableAction) {
            return findSequencesForSEFFSetVariableAction((SetVariableAction) currentAction, context, callers,
                    previousSequence);

        } else if (currentAction instanceof BranchAction) {
            return findSequencesForSEFFBranchAction((BranchAction) currentAction, context, callers, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("The type %s is not supported in SEFFs", currentAction.getClass()
                        .getName()));
        }
    }

    private List<ActionSequence> findSequencesForSEFFStartAction(StartAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers,
                previousSequence);
    }

    private List<ActionSequence> findSequencesForSEFFStopAction(StopAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class,
                false);

        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get()
                .getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor, context, callers, previousSequence);
        } else {
            AbstractPCMActionSequenceElement<?> caller = callers.removeLast();
            return returnToCaller(caller, callers, previousSequence);
        }
    }

    private List<ActionSequence> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {

        var callingEntity = new CallingSEFFActionSequenceElement(currentAction, context, true);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context);

        if (calledSEFF.isEmpty()) {
            return new ArrayList<ActionSequence>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .getSeff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                callers.add(callingEntity);
                return findSequencesForSEFFAction(SEFFStartAction.get(), calledSEFF.get()
                    .getContext(), callers, currentActionSequence);
            }
        }

    }

    private List<ActionSequence> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {

        var newEntity = new SEFFActionSequenceElement<>(currentAction, context);
        ActionSequence currentActionSequence = new ActionSequence(previousSequence, newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers,
                currentActionSequence);
    }

    private List<ActionSequence> findSequencesForSEFFBranchAction(BranchAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {

        return currentAction.getBranches_Branch()
            .stream()
            .map(AbstractBranchTransition::getBranchBehaviour_BranchTransition)
            .map(ResourceDemandingBehaviour::getSteps_Behaviour)
            .map(PCMQueryUtils::getFirstStartActionInActionList)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForSEFFAction(it, context, new ArrayDeque<>(callers), previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private List<ActionSequence> findSequencesForSEFFActionReturning(ExternalCallAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {
        ActionSequence currentActionSequence = new ActionSequence(previousSequence,
                new CallingSEFFActionSequenceElement(currentAction, context, false));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers,
                currentActionSequence);
    }

    private List<ActionSequence> returnToCaller(AbstractPCMActionSequenceElement<?> caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, ActionSequence previousSequence) {

        if (caller instanceof CallingUserActionSequenceElement) {
            return returnToUserCaller((CallingUserActionSequenceElement) caller, callers, previousSequence);

        } else if (caller instanceof CallingSEFFActionSequenceElement) {
            return returnToSEFFCaller((CallingSEFFActionSequenceElement) caller, callers, previousSequence);

        } else {
            throw new IllegalArgumentException(
                    String.format("No dispatch logic for call of type %s available.", caller.getClass()
                        .getSimpleName()));
        }
    }

    private List<ActionSequence> returnToUserCaller(CallingUserActionSequenceElement caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, ActionSequence previousSequence) {
        if (!callers.isEmpty()) {
            throw new IllegalStateException("Illegal state in action sequence finder.");
        } else {
            return findSequencesForUserActionReturning(caller.getElement(), previousSequence);
        }
    }

    private List<ActionSequence> returnToSEFFCaller(CallingSEFFActionSequenceElement caller,
            Deque<AbstractPCMActionSequenceElement<?>> callers, ActionSequence previousSequence) {
        return findSequencesForSEFFActionReturning(caller.getElement(), caller.getContext(), callers, previousSequence);
    }

} // TODO: There might be some refactoring potential as user and seff action handling has similiar
  // structure. Seems to violate the SRP. Solution might be, e.g., a facade or more utility classes
