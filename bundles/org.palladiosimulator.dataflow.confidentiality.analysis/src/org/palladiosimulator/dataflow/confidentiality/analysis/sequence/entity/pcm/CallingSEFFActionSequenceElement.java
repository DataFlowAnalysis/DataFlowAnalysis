package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFActionSequenceElement extends SEFFActionSequenceElement<ExternalCallAction>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingSEFFActionSequenceElement(ExternalCallAction element, Deque<AssemblyContext> context,
            boolean isCalling) {
        super(element, context);
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
