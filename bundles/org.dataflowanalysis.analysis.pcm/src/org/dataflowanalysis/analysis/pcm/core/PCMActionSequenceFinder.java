package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.PartialFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMUserFinderUtils;
import org.dataflowanalysis.analysis.pcm.utils.PCMQueryUtils;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMActionSequenceFinder implements PartialFlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMActionSequenceFinder.class);

    private final UsageModel usageModel;

    public PCMActionSequenceFinder(UsageModel usageModel) {
        this.usageModel = usageModel;
    }

    @Override
    public List<PCMPartialFlowGraph> findPartialFlowGraphs() {
    	List<PCMPartialFlowGraph> sequences = findSequencesForUsageModel(usageModel);
        logger.info(String.format("Found %d action %s.", sequences.size(),
                sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMPartialFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        PCMPartialFlowGraph initialList = new PCMPartialFlowGraph();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);

        return startActions.stream()
	        .map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialList))
	        .flatMap(List::stream)
	        .toList();
    }

}
