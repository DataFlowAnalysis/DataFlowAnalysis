package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.List;
import java.util.Stack;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {

    public SEFFActionSequenceElement(T element, Stack<AssemblyContext> context) {
        super(element, context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<DataFlowVariable> evaluateDataFlow(List<DataFlowVariable> variables) {
        // TODO Auto-generated method stub
        return null;
    }

}
