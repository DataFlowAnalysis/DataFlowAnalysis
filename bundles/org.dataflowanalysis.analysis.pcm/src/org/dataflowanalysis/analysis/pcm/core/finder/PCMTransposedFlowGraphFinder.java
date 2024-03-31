package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.TransposedFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.PCMTransposedFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMTransposedFlowGraphFinder implements TransposedFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMTransposedFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;

    public PCMTransposedFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public List<PCMTransposedFlowGraph> findTransposedFlowGraphs() {
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) this.resourceProvider;
        List<PCMTransposedFlowGraph> sequences = findSequencesForUsageModel(pcmResourceProvider.getUsageModel());
        logger.info(String.format("Found %d action %s.", sequences.size(), sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMTransposedFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream()
                .map(it -> new PCMUserTransposedFlowGraphFinder(this.resourceProvider).findSequencesForUserAction(it))
                .flatMap(List::stream).toList();
    }
}
