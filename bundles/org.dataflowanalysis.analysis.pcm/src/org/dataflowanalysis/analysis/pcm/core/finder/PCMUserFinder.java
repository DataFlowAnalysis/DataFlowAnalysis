package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
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

public class PCMUserFinder {
	
	private IUserPCMVertexFactory elementFactory;
	private final Logger logger = Logger.getLogger(PCMUserFinder.class);
	private PCMSEFFFinder seffFinder;
	
	protected PCMUserFinder(IUserPCMVertexFactory userElementFactory, PCMSEFFFinder seffFinder) {
		this.elementFactory = userElementFactory;
		this.seffFinder = seffFinder;
	}
	
	public PCMUserFinder(IUserPCMVertexFactory userElementFactory, ISEFFPCMVertexFactory seffElementFactory) {
		this.elementFactory = userElementFactory;
		this.seffFinder = new PCMSEFFFinder(seffElementFactory, this);
	}
	
	public List<PCMPartialFlowGraph> findSequencesForUserAction(AbstractUserAction currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        if (currentAction instanceof Start) {
            return findSequencesForUserStartAction((Start) currentAction, previousSequence, resourceProvider);

        } else if (currentAction instanceof Stop) {
            return findSequencesForUserStopAction((Stop) currentAction, previousSequence, resourceProvider);

        } else if (currentAction instanceof Branch) {
            return findSequencesForUserBranchAction((Branch) currentAction, previousSequence, resourceProvider);

        } else if (currentAction instanceof EntryLevelSystemCall) {
            return findSequencesForEntryLevelSystemCall((EntryLevelSystemCall) currentAction, previousSequence, resourceProvider);

        } else {
            // default case: skip action and continue with successor
            logger.info(
                    String.format("Action %s has unsupported type of %s and is skipped.", currentAction.getId(), currentAction.getClass().getName()));
            return findSequencesForUserAction(currentAction.getSuccessor(), previousSequence, resourceProvider);
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserStartAction(Start currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        UserPCMVertex<? extends AbstractUserAction> startElement;
        if (previousSequence.getSink() == null) {
            startElement = elementFactory.createStartElement(currentAction, resourceProvider);
        } else {
            startElement = elementFactory.createStartElement(currentAction, List.of(previousSequence.getSink()), resourceProvider);
        }
        var currentSequence = new PCMPartialFlowGraph(startElement);
        return findSequencesForUserAction(currentAction.getSuccessor(), currentSequence, resourceProvider);
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserStopAction(Stop currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        var stopElement = elementFactory.createStopElement(currentAction, List.of(previousSequence.getSink()), resourceProvider);
        var currentSequence = new PCMPartialFlowGraph(stopElement);

        Optional<AbstractUserAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractUserAction.class, false);
        if (parentAction.isEmpty()) {
            return List.of(currentSequence);
        } else {
            return findSequencesForUserAction(parentAction.get().getSuccessor(), currentSequence, resourceProvider);
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForUserBranchAction(Branch currentAction, PCMPartialFlowGraph previousSequence,
            ResourceProvider resourceProvider) {
        return currentAction.getBranchTransitions_Branch().stream().map(BranchTransition::getBranchedBehaviour_BranchTransition)
                .map(PCMQueryUtils::getStartActionOfScenarioBehavior).flatMap(Optional::stream)
                .map(it -> findSequencesForUserAction(it, previousSequence, resourceProvider)).flatMap(List::stream).toList();
    }

    protected List<PCMPartialFlowGraph> findSequencesForEntryLevelSystemCall(EntryLevelSystemCall currentAction,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider) {
        var callingEntity = elementFactory.createCallingElement(currentAction, List.of(previousSequence.getSink()), resourceProvider);
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(callingEntity);

        OperationProvidedRole calledRole = currentAction.getProvidedRole_EntryLevelSystemCall();
        OperationSignature calledSignature = currentAction.getOperationSignature__EntryLevelSystemCall();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, new ArrayDeque<>());

        if (calledSEFF.isEmpty()) {
            return new ArrayList<>();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get().seff().getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action.");
            } else {
                Deque<AbstractPCMVertex<?>> callers = new ArrayDeque<>();
                callers.add(callingEntity);

                SEFFFinderContext finderContext = new SEFFFinderContext(calledSEFF.get().context(), callers,
                        calledSignature.getParameters__OperationSignature());
                return seffFinder.findSequencesForSEFFAction(SEFFStartAction.get(), finderContext, currentActionSequence, resourceProvider);
            }
        }
    }

    public List<PCMPartialFlowGraph> findSequencesForUserActionReturning(EntryLevelSystemCall currentAction,
            PCMPartialFlowGraph previousSequence, ResourceProvider resourceProvider, AbstractPCMVertex<?> caller) {
        List<AbstractPCMVertex<?>> previousVertices = new ArrayList<>();
        previousVertices.add(caller);
        previousVertices.add(previousSequence.getSink());
        PCMPartialFlowGraph currentActionSequence = new PCMPartialFlowGraph(
                elementFactory.createReturningElement(currentAction, previousVertices, resourceProvider));
        return findSequencesForUserAction(currentAction.getSuccessor(), currentActionSequence, resourceProvider);
    }

}
