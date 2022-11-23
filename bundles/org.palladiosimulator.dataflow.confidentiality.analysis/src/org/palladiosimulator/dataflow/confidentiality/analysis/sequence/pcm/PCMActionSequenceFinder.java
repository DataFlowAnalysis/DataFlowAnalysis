package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder.PCMUserFinderUtils;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMActionSequenceFinder implements ActionSequenceFinder {
    private final Logger logger = Logger.getLogger(PCMActionSequenceFinder.class);

    private final UsageModel usageModel;
    private final Allocation allocationModel;

    public PCMActionSequenceFinder(UsageModel usageModel, Allocation allocationModel) {
        this.usageModel = usageModel;
        this.allocationModel = allocationModel;
    }

    @Override
    public List<ActionSequence> findAllSequences() {
    	List<ActionSequence> sequences = findSequencesForUsageModel(usageModel);
        logger.info(String.format("Found %d action %s.", sequences.size(),
                sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<ActionSequence> findSequencesForUsageModel(UsageModel usageModel) {
        ActionSequence initialList = new ActionSequence();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);
        List<DataStore> initialDataStores = new ArrayList<>();

        return startActions.stream()
        .map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialDataStores, initialList))
        .flatMap(List::stream)
        .toList();
    }

}
