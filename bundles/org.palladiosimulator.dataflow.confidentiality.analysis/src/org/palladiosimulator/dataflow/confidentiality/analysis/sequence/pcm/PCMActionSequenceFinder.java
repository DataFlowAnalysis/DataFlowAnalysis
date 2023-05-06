package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.finder.PCMUserFinderUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMActionSequenceFinder implements ActionSequenceFinder {
    private final Logger logger = Logger.getLogger(PCMActionSequenceFinder.class);

    private final UsageModel usageModel;

    public PCMActionSequenceFinder(UsageModel usageModel) {
        this.usageModel = usageModel;
    }

    @Override
    public List<PCMActionSequence> findAllSequences() {
    	List<PCMActionSequence> sequences = findSequencesForUsageModel(usageModel);
        logger.info(String.format("Found %d action %s.", sequences.size(),
                sequences.size() == 1 ? "sequence" : "sequences"));
        return sequences;
    }

    private List<PCMActionSequence> findSequencesForUsageModel(UsageModel usageModel) {
        PCMActionSequence initialList = new PCMActionSequence();
        List<Start> startActions = PCMQueryUtils.findStartActionsForUsageModel(usageModel);
        List<DataStore> initialDataStores = new ArrayList<>();

        return startActions.parallelStream()
        .map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialDataStores, initialList))
        .flatMap(List::parallelStream)
        .toList();
    }

}
