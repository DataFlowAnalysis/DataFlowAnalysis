package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.UserActionSequenceElement;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMActionSequenceFinder implements ActionSequenceFinder {

    private final UsageModel usageModel;
    private final Allocation allocationModel;

    private final Logger logger = Logger.getLogger(PCMActionSequenceFinder.class);

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
        return findStartActionsForUsageModel(usageModel).stream()
            .map(this::findActionSequencesForUserAction)
            .flatMap(List::stream)
            .toList();
    }

    private List<Start> findStartActionsForUsageModel(UsageModel usageModel) {
        return usageModel.getUsageScenario_UsageModel()
            .stream()
            .map(PCMQueryUtils::getStartActionOfScenarioBehavior)
            .flatMap(Optional::stream)
            .toList();
    }

    private List<ActionSequence> findActionSequencesForUserAction(Start startAction) {
        // TODO: Continue impl
        ActionSequence sequence = new ActionSequence();
        sequence.addElement(new UserActionSequenceElement<Start>(startAction));
        return List.of(sequence);
    }

}
