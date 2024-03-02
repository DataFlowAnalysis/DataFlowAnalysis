package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.pcm.utils.SEFFWithContext;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
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

public class PCMSEFFPartialFlowGraphFinder {
    private static final Logger logger = Logger.getLogger(PCMSEFFPartialFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;
    private final SEFFFinderContext context;
    private PCMPartialFlowGraph currentPartialFlowGraph;

    public PCMSEFFPartialFlowGraphFinder(ResourceProvider resourceProvider, SEFFFinderContext context, PCMPartialFlowGraph currentPartialFlowGraph) {
        this.resourceProvider = resourceProvider;
        this.context = context;
        this.currentPartialFlowGraph = currentPartialFlowGraph;
    }

    public List<PCMPartialFlowGraph> findSequencesForSEFFAction(AbstractAction currentAction) {

        if (currentAction instanceof StartAction) {
            return findSequencesForSEFFStartAction((StartAction) currentAction);

        } else if (currentAction instanceof StopAction) {
            return findSequencesForSEFFStopAction((StopAction) currentAction);

        } else if (currentAction instanceof ExternalCallAction) {
            return findSequencesForSEFFExternalCallAction((ExternalCallAction) currentAction);

        } else if (currentAction instanceof SetVariableAction) {
            return findSequencesForSEFFSetVariableAction((SetVariableAction) currentAction);

        } else if (currentAction instanceof BranchAction) {
            return findSequencesForSEFFBranchAction((BranchAction) currentAction);

        } else {
            // default case: skip action and continue with successor
            logger.info(
                    String.format("Action %s has unsupported type of %s and is skipped.", currentAction.getId(), currentAction.getClass().getName()));
            return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction());
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFStartAction(StartAction currentAction) {
        var startElement = new SEFFPCMVertex<>(currentAction, List.of(this.currentPartialFlowGraph.getSink()),
                context.getContext(), context.getParameter(), resourceProvider);
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(startElement);
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction());
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFStopAction(StopAction currentAction) {
        var stopElement = new SEFFPCMVertex<>(currentAction, List.of(this.currentPartialFlowGraph.getSink()),
                context.getContext(), context.getParameter(), resourceProvider);
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(stopElement);

        Optional<AbstractAction> parentAction = PCMQueryUtils.findParentOfType(currentAction, AbstractAction.class, false);
        if (parentAction.isPresent()) {
            AbstractAction successor = parentAction.get().getSuccessor_AbstractAction();
            return findSequencesForSEFFAction(successor);
        } else {
            AbstractVertex<?> caller = context.getLastCaller();
            context.updateParameterForCallerReturning(caller);
            return returnToCaller(caller);
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFExternalCallAction(ExternalCallAction currentAction) {

        var callingEntity = new CallingSEFFPCMVertex(currentAction, List.of(this.currentPartialFlowGraph.getSink()), context.getContext(), context.getParameter(),
                true, resourceProvider);
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(callingEntity);

        OperationRequiredRole calledRole = currentAction.getRole_ExternalService();
        OperationSignature calledSignature = currentAction.getCalledService_ExternalService();
        Optional<SEFFWithContext> calledSEFF = PCMQueryUtils.findCalledSEFF(calledRole, calledSignature, context.getContext());

        if (calledSEFF.isEmpty()) {
            logger.error(String.format("Could not find the called SEFF for the action %s", currentAction));
            throw new IllegalStateException();
        } else {
            Optional<StartAction> SEFFStartAction = PCMQueryUtils.getFirstStartActionInActionList(calledSEFF.get().seff().getSteps_Behaviour());

            if (SEFFStartAction.isEmpty()) {
                throw new IllegalStateException("Unable to find SEFF start action");
            } else {
                context.addCaller(callingEntity);
                context.updateSEFFContext(calledSEFF.get().context());
                context.updateParametersForCall(calledSignature.getParameters__OperationSignature());

                return findSequencesForSEFFAction(SEFFStartAction.get());
            }
        }
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFSetVariableAction(SetVariableAction currentAction) {

        var newEntity = new SEFFPCMVertex<>(currentAction, List.of(this.currentPartialFlowGraph.getSink()), context.getContext(), context.getParameter(),
                resourceProvider);
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(newEntity);

        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction());
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFBranchAction(BranchAction currentAction) {
        return currentAction.getBranches_Branch().stream().map(AbstractBranchTransition::getBranchBehaviour_BranchTransition)
                .map(ResourceDemandingBehaviour::getSteps_Behaviour).map(PCMQueryUtils::getFirstStartActionInActionList).flatMap(Optional::stream)
                .map(it -> {
                    Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping = new IdentityHashMap<>();
                    PCMPartialFlowGraph clonedPartialFlowGraph = this.currentPartialFlowGraph.deepCopy(vertexMapping);
                    SEFFFinderContext clonedContext = new SEFFFinderContext(context);
                    clonedContext.replaceCallers(vertexMapping);
                    return new PCMSEFFPartialFlowGraphFinder(resourceProvider, clonedContext, clonedPartialFlowGraph).findSequencesForSEFFAction(it);
                }).flatMap(List::stream).toList();
    }

    protected List<PCMPartialFlowGraph> findSequencesForSEFFActionReturning(ExternalCallAction currentAction, AbstractPCMVertex<?> caller) {
        List<AbstractPCMVertex<?>> previousVertices = new ArrayList<>();
        previousVertices.add(caller);
        previousVertices.add(this.currentPartialFlowGraph.getSink());
        this.currentPartialFlowGraph = new PCMPartialFlowGraph(
                new CallingSEFFPCMVertex(currentAction, previousVertices, context.getContext(), context.getParameter(), false, resourceProvider));
        return findSequencesForSEFFAction(currentAction.getSuccessor_AbstractAction());
    }

    public List<PCMPartialFlowGraph> returnToCaller(AbstractVertex<?> caller) {
        if (caller instanceof CallingUserPCMVertex) {
            return returnToUserCaller((CallingUserPCMVertex) caller);

        } else if (caller instanceof CallingSEFFPCMVertex) {
            return returnToSEFFCaller((CallingSEFFPCMVertex) caller);

        } else {
            throw new IllegalArgumentException(String.format("No dispatch logic for call of type %s available.", caller.getClass().getSimpleName()));
        }
    }

    protected List<PCMPartialFlowGraph> returnToUserCaller(CallingUserPCMVertex caller) {
        if (!this.context.getCallers().isEmpty()) {
            logger.error("SEFF Action wanted to return without a matching calling user sequence element");
            throw new IllegalStateException();
        } else {
            return new PCMUserPartialFlowGraphFinder(resourceProvider, this.currentPartialFlowGraph)
                    .findSequencesForUserActionReturning(caller.getReferencedElement(), caller);
        }
    }

    protected List<PCMPartialFlowGraph> returnToSEFFCaller(CallingSEFFPCMVertex caller) {
        context.updateSEFFContext(caller.getContext());
        return findSequencesForSEFFActionReturning(caller.getReferencedElement(), caller);
    }
}
