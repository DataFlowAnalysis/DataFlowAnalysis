package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.PartialFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMPartialFlowGraphFinder implements PartialFlowGraphFinder {
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
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream()
                .map(it -> new PCMUserPartialFlowGraphFinder(this.resourceProvider).findSequencesForUserAction(it))
                .flatMap(List::stream).toList();
    }
}
