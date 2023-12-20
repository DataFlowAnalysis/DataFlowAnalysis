package org.dataflowanalysis.analysis.core.pcm;

import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.ActionSequenceFinder;
import org.dataflowanalysis.analysis.core.pcm.finder.PCMUserFinderUtils;
import org.dataflowanalysis.analysis.utils.pcm.PCMQueryUtils;
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

        return startActions.stream()
	        .map(it -> PCMUserFinderUtils.findSequencesForUserAction(it, initialList))
	        .flatMap(List::stream)
	        .toList();
    }

}
