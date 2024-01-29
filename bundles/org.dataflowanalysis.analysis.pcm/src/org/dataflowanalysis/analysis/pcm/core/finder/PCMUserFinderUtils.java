package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.pcm.utils.SEFFWithContext;
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
	private static final Logger logger = Logger.getLogger(PCMUserFinderUtils.class);

	private PCMUserFinderUtils() {
		// Utility class
	}
	
	public static List<PCMPartialFlowGraph> findSequencesForUserAction(AbstractUserAction currentAction, PCMPartialFlowGraph previousSequence) {
        if (currentAction instanceof Start) {
            return findSequencesForUserStartAction((Start) currentAction, previousSequence);

        } else if (currentAction instanceof Stop) {
            return findSequencesForUserStopAction((Stop) currentAction, previousSequence);

        } else if (currentAction instanceof Branch) {
            return findSequencesForUserBranchAction((Branch) currentAction, previousSequence);

        } else if (currentAction instanceof EntryLevelSystemCall) {
            return findSequencesForEntryLevelSystemCall((EntryLevelSystemCall) currentAction, previousSequence);

        } else {
        	// default case: skip action and continue with successor
        	logger.info(String.format("Action %s has unsupported type of %s and is skipped.", 
        			currentAction.getId(), currentAction.getClass().getName()));
        	return findSequencesForUserAction(currentAction.getSuccessor(), previousSequence);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserStartAction(Start currentAction,
            PCMPartialFlowGraph previousSequence) {
    	var startElement = new UserPCMVertex<Start>(currentAction);
        var currentSequence = new PCMPartialFlowGraph(previousSequence, startElement);
        return findSequencesForUserAction(currentAction.getSuccessor(), currentSequence);
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserStopAction(Stop currentAction,
            PCMPartialFlowGraph previousSequence) {
    	var stopElement = new UserPCMVertex<Stop>(currentAction);
        var currentSequence = new PCMPartialFlowGraph(previousSequence, stopElement);
    	
        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction,
                AbstractUserAction.class, false);
        if (parentAction.isEmpty()) {
            return List.of(currentSequence);
        } else {
            return findSequencesForUserAction(parentAction.get()
                .getSuccessor(), currentSequence);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserBranchAction(Branch currentAction, PCMPartialFlowGraph previousSequence) {
        return currentAction.getBranchTransitions_Branch()
            .stream()
            .map(BranchTransition::getBranchedBehaviour_BranchTransition)
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .map(it -> findSequencesForUserAction(it, previousSequence))
            .flatMap(List::stream)
            .toList();
    }

    private static List<PCMPartialFlowGraph> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction, PCMPartialFlowGraph previousSequence) {
        var callingEntity = new CallingUserPCMVertex(currentAction, true);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(previousSequence, callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature,
                new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<PCMPartialFlowGraph>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                .seff()
                .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMVertex<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers, calledSignature.getParameters__OperationSignature());
                return PCMSEFFFinderUtils.findSequencesForSEFFAction(SEFFStartAction.get(), finderContext ,currentActionSequence);
            }
        }
    }

    public static List<PCMPartialFlowGraph> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction, PCMPartialFlowGraph previousSequence) {
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(previousSequence,
                new CallingUserPCMVertex(currentAction, false));
        return findSequencesForUserAction(currentAction.getSuccessor(), currentActionSequence);
    }
}
