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
import org.dataflowanalysis.analysis.resource.ResourceProvider;
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

    public static List<PCMPartialFlowGraph> findSequencesForUserAction(AbstractUserAction currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        switch (currentAction) {
            case Start start -> {
                return findSequencesForUserStartAction(start, previousSequence, resourceProvider);
            }
            case Stop stop -> {
                return findSequencesForUserStopAction(stop, previousSequence, resourceProvider);
            }
            case Branch branch -> {
                return findSequencesForUserBranchAction(branch, previousSequence, resourceProvider);
            }
            case EntryLevelSystemCall entryLevelSystemCall -> {
                return findSequencesForEntryLevelSystemCall(entryLevelSystemCall, previousSequence, resourceProvider);
            }
            case null, default -> {
                // default case: skip action and continue with successor
                logger.info(
                        String.format("Action %s has unsupported type of %s and is skipped.", currentAction.getId(), currentAction.getClass().getName()));
                return findSequencesForUserAction(currentAction.getSuccessor(), previousSequence, resourceProvider);
            }
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserStartAction(Start currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        UserPCMVertex<? extends AbstractUserAction> startElement;
        if (previousSequence.getSink() == null) {
            startElement = new UserPCMVertex<Start>(currentAction, resourceProvider);
        } else {
            startElement = new UserPCMVertex<Start>(currentAction, List.of(previousSequence.getSink()), resourceProvider);
        }
        var currentSequence = new PCMPartialFlowGraph(startElement);
        return findSequencesForUserAction(currentAction.getSuccessor(), currentSequence, resourceProvider);
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserStopAction(Stop currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        var stopElement = new UserPCMVertex<Stop>(currentAction, List.of(previousSequence.getSink()), resourceProvider);
        var currentSequence = new PCMPartialFlowGraph(stopElement);

        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractUserAction.class, false);
        if (parentAction.isEmpty()) {
            return List.of(currentSequence);
        } else {
            return findSequencesForUserAction(parentAction.get().getSuccessor(), currentSequence, resourceProvider);
        }
    }

    private static List<PCMPartialFlowGraph> findSequencesForUserBranchAction(Branch currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        return currentAction.getBranchTransitions_Branch().stream().map(BranchTransition::getBranchedBehaviour_BranchTransition)
                .map(PCMQueryUtils::getStartActionOfScenarioBehavior).flatMap(Optional::stream)
                .map(it -> findSequencesForUserAction(it, previousSequence, resourceProvider)).flatMap(List::stream).toList();
    }

    private static List<PCMPartialFlowGraph> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        var callingEntity = new CallingUserPCMVertex(currentAction, List.of(previousSequence.getSink()), true, resourceProvider);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<PCMPartialFlowGraph>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get().seff().getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMVertex<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers,
                        calledSignature.getParameters__OperationSignature());
                return PCMSEFFFinderUtils.findSequencesForSEFFAction(SEFFStartAction.get(), finderContext, currentActionSequence, resourceProvider);
            }
        }
    }

    public static List<PCMPartialFlowGraph> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider, AbstractPCMVertex<?> caller) {
        List<AbstractPCMVertex<?>> previousVertices = new ArrayList<>();
        previousVertices.add(caller);
        previousVertices.add(previousSequence.getSink());
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(
                new CallingUserPCMVertex(currentAction, previousVertices, false, resourceProvider));
        return findSequencesForUserAction(currentAction.getSuccessor(), currentActionSequence, resourceProvider);
    }
}
