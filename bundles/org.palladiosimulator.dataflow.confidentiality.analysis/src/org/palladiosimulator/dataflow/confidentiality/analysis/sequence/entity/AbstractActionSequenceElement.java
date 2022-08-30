package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;

    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
    }

    public AbstractActionSequenceElement(List<DataFlowVariable> variables) {
        this.dataFlowVariables = Optional.of(List.copyOf(variables));
    }

    public abstract AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables);

    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.dataFlowVariables.orElseThrow(IllegalStateException::new);
    }

    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
    }

    @Override
    public abstract String toString();

}
