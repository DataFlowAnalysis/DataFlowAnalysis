package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFWithContext;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.StartAction;
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
            return findSequenceForUserStartAction((Start) currentAction, previousSequence);

        } else if (currentAction instanceof Stop) {
            return findSequenceForUserStopAction((Stop) currentAction, previousSequence);

        } else if (currentAction instanceof Branch) {
            return findSequenceForUserBranchAction((Branch) currentAction, previousSequence);

        } else if (currentAction instanceof EntryLevelSystemCall) {
            return findSequenceForEntryLevelSystemCall((EntryLevelSystemCall) currentAction, previousSequence);

        } else {
            logger.warn(String.format("The type %s is not supported in usage scenarios.", currentAction.getClass()
                .getName()));
            return null;
        }
    }

    private List<ActionSequence> findSequenceForUserStartAction(Start currentAction, ActionSequence previousSequence) {
        return findSequencesForUserAction(currentAction.getSuccessor(), previousSequence);
    }

    private List<ActionSequence> findSequenceForUserStopAction(Stop currentAction, ActionSequence previousSequence) {
        AbstractUserAction parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractUserAction.class,
                false);

        if (parentAction == null) {
            return List.of(previousSequence);
        } else {
            return findSequencesForUserAction(parentAction.getSuccessor(), previousSequence);
        }
    }

    private List<ActionSequence> findSequenceForUserBranchAction(Branch currentAction,
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

    private List<ActionSequence> findSequenceForEntryLevelSystemCall(EntryLevelSystemCall currentAction,
            ActionSequence previousSequence) {
        var callingEntity = new CallingUserActionSequenceElement(currentAction, true);
        previousSequence.addElement(callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature,
                new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<ActionSequence>();
        } else {
            Optional<StartAction> SEFFStartAction = calledSEFF.get()
                .getSeff()
                .getSteps_Behaviour()
                .stream()
                .filter(it -> it instanceof StartAction)
                .map(it -> (StartAction) it)
                .findFirst();

            if (SEFFStartAction.isEmpty()) {
                logger.warn("Unable to find SEFF start action.");
                return new ArrayList<ActionSequence>();
            } else {
                Deque<AbstractPCMActionSequenceElement<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                return findSequencesForSEFFAction(SEFFStartAction.get(), calledSEFF.get()
                    .getContext(), callers, previousSequence);
            }
        }
    }

    private List<ActionSequence> findSequencesForSEFFAction(AbstractAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {
        if (currentAction instanceof StartAction) {
            return findSequencesForSEFFStartAction((StartAction) currentAction, context, callers, previousSequence);
            // TODO: Implement others
        } else {
            logger.warn(String.format("The type %s is not supported in SEFFs", currentAction.getClass()
                .getName()));
            return null;
        }
    }

    private List<ActionSequence> findSequencesForSEFFStartAction(StartAction currentAction,
            Deque<AssemblyContext> context, Deque<AbstractPCMActionSequenceElement<?>> callers,
            ActionSequence previousSequence) {
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction(), context, callers,
                previousSequence);
    }

}
