package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMUserFinderUtils;
import org.dataflowanalysis.analysis.pcm.flowgraph.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMPartialFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMPartialFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;

    public PCMPartialFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public List<PCMPartialFlowGraph> findPartialFlowGraphs() {
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) this.resourceProvider;
        List<PCMPartialFlowGraph> sequences = findSequencesForUsageModel(pcmResourceProvider.getUsageModel());
        logger.info(String.format("Found %d action %s.", sequences.size(), sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMPartialFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        PCMPartialFlowGraph initialList = new PCMPartialFlowGraph();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream().map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialList, this.resourceProvider))
                .flatMap(List::stream).toList();
    }
}
