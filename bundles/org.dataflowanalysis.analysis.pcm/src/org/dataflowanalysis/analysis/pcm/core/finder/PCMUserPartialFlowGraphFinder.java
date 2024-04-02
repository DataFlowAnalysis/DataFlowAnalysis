package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.*;
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

public class PCMUserPartialFlowGraphFinder {
    private static final Logger logger = Logger.getLogger(PCMUserPartialFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;
    private PCMPartialFlowGraph currentPartialFlowGraph;

    public PCMUserPartialFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.currentPartialFlowGraph = new PCMPartialFlowGraph();
    }

    public PCMUserPartialFlowGraphFinder(ResourceProvider resourceProvider, PCMPartialFlowGraph currentPartialFlowGraph) {
        this.resourceProvider = resourceProvider;
        this.currentPartialFlowGraph = currentPartialFlowGraph;
    }

    public List<PCMPartialFlowGraph> findSequencesForUserAction(AbstractUserAction initialAction) {
        if (initialAction instanceof Start) {
            return findSequencesForUserStartAction((Start) initialAction);

        } else if (initialAction instanceof Stop) {
            return findSequencesForUserStopAction((Stop) initialAction);

        } else if (initialAction instanceof Branch) {
            return findSequencesForUserBranchAction((Branch) initialAction);

        } else if (initialAction instanceof EntryLevelSystemCall) {
            return findSequencesForEntryLevelSystemCall((EntryLevelSystemCall) initialAction);

        } else {
            // default case: skip action and continue with successor
            logger.info(String.format("Action %s has unsupported type of %s and is skipped.", initialAction.getId(), initialAction.getClass()
                    .getName()));
            return findSequencesForUserAction(initialAction.getSuccessor());
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserStartAction(Start currentAction) {
        UserPCMVertex<? extends AbstractUserAction> startElement;
        if (this.currentPartialFlowGraph.getSink() == null) {
            startElement = new UserPCMVertex<>(currentAction, resourceProvider);
        } else {
            startElement = new UserPCMVertex<>(currentAction, List.of(this.currentPartialFlowGraph.getSink()), resourceProvider);
        }
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(startElement);
        return findSequencesForUserAction(currentAction.getSuccessor());
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserStopAction(Stop currentAction) {
        var stopElement = new UserPCMVertex<>(currentAction, List.of(this.currentPartialFlowGraph.getSink()), resourceProvider);

        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractUserAction.class, false);
        if (parentAction.isEmpty()) {
            return List.of(new PCMPartialFlowGraph(stopElement));
        } else {
            this.currentPartialFlowGraph = new PCMPartialFlowGraph(stopElement);
            return findSequencesForUserAction(parentAction.get()
                    .getSuccessor());
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserBranchAction(Branch currentAction) {
        return currentAction.getBranchTransitions_Branch()
                .stream()
                .map(BranchTransition::getBranchedBehaviour_BranchTransition)
                .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
                .flatMap(Optional::stream)
                .map(it -> {
                    Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping = new IdentityHashMap<>();
                    PCMPartialFlowGraph clonedSequence = this.currentPartialFlowGraph.deepCopy(vertexMapping);
                    return new PCMUserPartialFlowGraphFinder(this.resourceProvider, clonedSequence).findSequencesForUserAction(it);
                })
                .flatMap(List::stream)
                .toList();
    }

    protected List<PCMPartialFlowGraph> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction) {
        var callingEntity = new CallingUserPCMVertex(currentAction, List.of(this.currentPartialFlowGraph.getSink()), true, resourceProvider);
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            logger.error(String.format("Could not find the called SEFF for the action %s", currentAction));
            throw new IllegalStateException();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get()
                    .seff()
                    .getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action");
            } else {
                Deque<AbstractPCMVertex<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get()
                        .context(), callers, calledSignature.getParameters__OperationSignature());
                return new PCMSEFFPartialFlowGraphFinder(resourceProvider, finderContext, this.currentPartialFlowGraph)
                        .findSequencesForSEFFAction(SEFFStartAction.get());
            }
        }
    }

    public List<PCMPartialFlowGraph> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction, AbstractPCMVertex<?> caller) {
        List<AbstractPCMVertex<?>> previousVertices = new ArrayList<>();
        previousVertices.add(caller);
        previousVertices.add(this.currentPartialFlowGraph.getSink());
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(new CallingUserPCMVertex(currentAction, previousVertices, false, resourceProvider));
        return findSequencesForUserAction(currentAction.getSuccessor());
    }
}
