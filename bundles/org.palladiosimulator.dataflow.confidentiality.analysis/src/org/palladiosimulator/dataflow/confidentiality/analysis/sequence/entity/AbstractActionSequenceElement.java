package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;

    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
    }

    public abstract List<DataFlowVariable> evaluateDataFlow(List<DataFlowVariable> variables);

    public Optional<List<DataFlowVariable>> getAllDataFlowVariables() {
        return this.dataFlowVariables;
    }

    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
    }

}
