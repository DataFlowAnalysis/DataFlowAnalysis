package org.dataflowanalysis.analysis.pcm.core.finder;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.PCMTransposeFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMTransposeFlowGraphFinder implements TransposeFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMTransposeFlowGraphFinder.class);

    private final ResourceProvider resourceProvider;

    public PCMTransposeFlowGraphFinder(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public List<PCMTransposeFlowGraph> findTransposeFlowGraphs() {
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) this.resourceProvider;
        List<PCMTransposeFlowGraph> sequences = findSequencesForUsageModel(pcmResourceProvider.getUsageModel());
        logger.info(String.format("Found %d action %s.", sequences.size(), sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMTransposeFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream()
                .map(it -> new PCMUserTransposeFlowGraphFinder(this.resourceProvider).findSequencesForUserAction(it))
                .flatMap(List::stream).toList();
    }
}
