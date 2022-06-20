package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.List;
import java.util.Stack;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public class UserActionSequenceElement<T extends AbstractUserAction> extends AbstractPCMActionSequenceElement<T> {

    public UserActionSequenceElement(T element) {
        super(element, new Stack<>());
    }

    @Override
    public List<DataFlowVariable> evaluateDataFlow(List<DataFlowVariable> variables) {
        // TODO Auto-generated method stub
        return null;
    }

}
