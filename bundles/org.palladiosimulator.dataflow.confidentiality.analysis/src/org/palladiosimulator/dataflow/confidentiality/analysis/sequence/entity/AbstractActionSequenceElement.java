package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;
    private final Optional<List<CharacteristicValue>> nodeVariables;

    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
        this.nodeVariables = Optional.empty();
    }

    public AbstractActionSequenceElement(List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        this.dataFlowVariables = Optional.of(List.copyOf(dataFlowVariables));
        this.nodeVariables = Optional.of(List.copyOf(nodeVariables));
    }

    public abstract AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables);

    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.dataFlowVariables.orElseThrow(IllegalStateException::new);
    }
    
    public List<CharacteristicValue> getAllNodeVariables() {
    	return this.nodeVariables.orElseThrow(IllegalStateException::new);
    }

    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
    }

    @Override
    public abstract String toString();

}
