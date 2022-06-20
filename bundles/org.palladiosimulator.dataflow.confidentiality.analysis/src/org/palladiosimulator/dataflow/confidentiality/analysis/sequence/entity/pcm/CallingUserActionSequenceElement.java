package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingUserActionSequenceElement(EntryLevelSystemCall element, boolean isCalling) {
        super(element);
        this.isCalling = isCalling;
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }

    // TODO: Custom hash and equals required?

    @Override
    public List<DataFlowVariable> evaluateDataFlow(List<DataFlowVariable> variables) {
        // TODO Auto-generated method stub
        return null;
    }

}
