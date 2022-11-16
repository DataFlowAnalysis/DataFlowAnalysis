package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class UserActionSequenceElement<T extends AbstractUserAction> extends AbstractPCMActionSequenceElement<T> {

    public UserActionSequenceElement(T element) {
        super(element, new ArrayDeque<>(), List.of());
    }

    public UserActionSequenceElement(UserActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
        List<DataFlowVariable> dataFlowVariables = this.evaluateDataFlowCharacteristics(variables, nodeCharacteristics);
        return new UserActionSequenceElement<T>(this, dataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public List<DataFlowVariable> getAvailableDataFlowVariables(List<DataFlowVariable> variables) {
    	return variables;
    }
    
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	var usageScenario = PCMQueryUtils.findParentOfType(this.getElement(), UsageScenario.class, false).get();
    	return this.evaluateNodeCharacteristics(usageScenario);
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
