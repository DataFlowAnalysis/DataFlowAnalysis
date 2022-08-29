package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    // TODO: Test with more immutable approaches like records classes
    private final Optional<List<DataFlowVariable>> dataFlowVariables;

    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
    }

    public abstract AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables);

    public Optional<List<DataFlowVariable>> getAllDataFlowVariables() {
        return this.dataFlowVariables;
    }

    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
    }
    
    @Override
    public abstract String toString();

}
