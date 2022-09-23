package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.seff.AbstractAction;

public class SEFFActionSequenceElement<T extends AbstractAction> extends AbstractPCMActionSequenceElement<T> {

    public SEFFActionSequenceElement(T element, Deque<AssemblyContext> context) {
        super(element, context);
        // TODO Auto-generated constructor stub
    }
    
    public SEFFActionSequenceElement(SEFFActionSequenceElement<T> oldElement, List<DataFlowVariable> variables) {
    	super(oldElement, variables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	System.out.println("Skipping element: " + this.getElement().getEntityName());
    	return new SEFFActionSequenceElement<>(this, variables);
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
