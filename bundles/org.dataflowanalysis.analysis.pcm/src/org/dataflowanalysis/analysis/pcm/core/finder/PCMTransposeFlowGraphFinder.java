package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.PCMTransposeFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMTransposeFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;

    public PCMTransposeFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs() {
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) this.resourceProvider;
        return this.findTransposeFlowGraphs(List.of(), PCMQueryUtils.findStartActionsForUsageModel(pcmResourceProvider.getUsageModel()));
    }
    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs(List<?> sourceNodes) {
        return this.findTransposeFlowGraphs(sourceNodes, List.of());
    }

    // TODO: Sink nodes not yet implemented, Sources may fail due to insufficient context (e.g. returns/parameters in seff)
    @Override
    public List<? extends PCMTransposeFlowGraph> findTransposeFlowGraphs(List<?> sinkNodes, List<?> sourceNodes) {
        List<PCMTransposeFlowGraph> transposeFlowGraphs = new ArrayList<>();
        List<AbstractUserAction> userActions = sourceNodes.stream()
                .filter(AbstractUserAction.class::isInstance)
                .map(AbstractUserAction.class::cast)
                .toList();
        List<AbstractAction> seffActions = sourceNodes.stream()
                .filter(AbstractAction.class::isInstance)
                .map(AbstractAction.class::cast)
                .toList();

        transposeFlowGraphs.addAll(this.findTransposeFlowGraphsForUserActions(userActions));
        transposeFlowGraphs.addAll(this.findTransposeFlowGraphsForSEFFActions(seffActions));
        logger.info(String.format("Found %d transpose flow %s.", transposeFlowGraphs.size(), transposeFlowGraphs.size() == 1 ? "graph" : "graphs"));
        return transposeFlowGraphs;
    }

    private List<PCMTransposeFlowGraph> findTransposeFlowGraphsForUserActions(List<AbstractUserAction> userActions) {
        return userActions.stream()
                .map(it -> new PCMUserTransposeFlowGraphFinder(this.resourceProvider).findSequencesForUserAction(it))
                .flatMap(List::stream)
                .toList();
    }

    private List<PCMTransposeFlowGraph> findTransposeFlowGraphsForSEFFActions(List<AbstractAction> seffActions) {
        SEFFFinderContext context = new SEFFFinderContext(new ArrayDeque<>(), new ArrayDeque<>(), new ArrayList<>());
        return seffActions.stream()
                .map(it -> new PCMSEFFTransposeFlowGraphFinder(this.resourceProvider, context, new PCMTransposeFlowGraph()).findSequencesForSEFFAction(it))
                .flatMap(List::stream)
                .toList();
    }
}
