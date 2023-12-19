package org.dataflowanalysis.analysis.sequence.pcm.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.dataflowanalysis.analysis.entity.pcm.AbstractPCMActionSequenceElement;
import org.dataflowanalysis.analysis.entity.pcm.PCMActionSequence;
import org.dataflowanalysis.analysis.entity.pcm.user.CallingUserActionSequenceElement;
import org.dataflowanalysis.analysis.entity.pcm.user.UserActionSequenceElement;
import org.dataflowanalysis.analysis.sequence.pcm.SEFFWithContext;
import org.dataflowanalysis.analysis.utils.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.repository.OperationProvidedRole;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Branch;
import org.palladiosimulator.pcm.usagemodel.BranchTransition;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class PCMUserFinderUtils {
	private PCMUserFinderUtils() {
		// Utility class
	}
	
	public static List<PCMActionSequence> findSequencesForUserAction(AbstractUserAction currentAction, PCMActionSequence previousSequence) {
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

    private static List<PCMActionSequence> findSequencesForUserStartAction(Start currentAction,
            PCMActionSequence previousSequence) {
    	var startElement = new UserActionSequenceElement<Start>(currentAction);
        var currentSequence = new PCMActionSequence(previousSequence, startElement);
        return findSequencesForUserAction(currentAction.getSuccessor(), currentSequence);
    }

    private static List<PCMActionSequence> findSequencesForUserStopAction(Stop currentAction,
            PCMActionSequence previousSequence) {
    	var stopElement = new UserActionSequenceElement<Stop>(currentAction);
        var currentSequence = new PCMActionSequence(previousSequence, stopElement);
    	
        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction,
                AbstractUserAction.class, false);
        if (parentAction.isEmpty()) {
            return List.of(currentSequence);
        } else {
            return findSequencesForUserAction(parentAction.get()
                .getSuccessor(), currentSequence);
        }
    }

    private static List<PCMActionSequence> findSequencesForUserBranchAction(Branch currentAction, PCMActionSequence previousSequence) {
        return currentAction.getBranchTransitions_Branch()
            .stream()
            .map(BranchTransition::getBranchedBehaviour_BranchTransition)
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForUserAction(it, previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private static List<PCMActionSequence> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction, PCMActionSequence previousSequence) {
        var callingEntity = new CallingUserActionSequenceElement(currentAction, true);
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence, callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature,
                new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<PCMActionSequence>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .seff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMActionSequenceElement<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers, calledSignature.getParameters__OperationSignature());
                return PCMSEFFFinderUtils.findSequencesForSEFFAction(SEFFStartAction.get(), finderContext ,currentActionSequence);
            }
        }
    }

    public static List<PCMActionSequence> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction, PCMActionSequence previousSequence) {
        PCMActionSequence currentActionSequence = new PCMActionSequence(previousSequence,
                new CallingUserActionSequenceElement(currentAction, false));
        return findSequencesForUserAction(currentAction.getSuccessor(), currentActionSequence);
    }
}
