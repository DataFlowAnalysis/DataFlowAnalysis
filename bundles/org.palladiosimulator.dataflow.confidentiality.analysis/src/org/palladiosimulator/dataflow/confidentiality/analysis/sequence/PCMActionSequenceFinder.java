package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

public class PCMActionSequenceFinder implements ActionSequenceFinder {
    
    private final UsageModel usageModel;
    private final Allocation allocationModel;

    public PCMActionSequenceFinder(UsageModel usageModel, Allocation allocationModel) {
        this.usageModel = usageModel;
        this.allocationModel = allocationModel;
    }

    @Override
    public List<ActionSequence> findAllSequences() {
        // TODO Auto-generated method stub
        return null;
    }

}
