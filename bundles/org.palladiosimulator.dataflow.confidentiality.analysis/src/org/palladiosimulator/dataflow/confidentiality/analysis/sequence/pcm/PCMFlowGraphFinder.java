package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMFlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.FlowGraphFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder.PCMUserFinderUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMFlowGraphFinder implements FlowGraphFinder {
    private final Logger logger = Logger.getLogger(PCMFlowGraphFinder.class);

    private final UsageModel usageModel;

    public PCMFlowGraphFinder(UsageModel usageModel) {
        this.usageModel = usageModel;
    }

    @Override
    public List<PCMFlowGraph> findAllSequences() {
    	List<PCMFlowGraph> sequences = findSequencesForUsageModel(usageModel);
        logger.info(String.format("Found %d action %s.", sequences.size(),
                sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMFlowGraph> findSequencesForUsageModel(UsageModel usageModel) {
        PCMFlowGraph initialList = new PCMFlowGraph();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);
        List<DataStore> initialDataStores = new ArrayList<>();

        return startActions.stream()
        .map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialDataStores, initialList))
        .flatMap(List::stream)
        .toList();
    }

}
