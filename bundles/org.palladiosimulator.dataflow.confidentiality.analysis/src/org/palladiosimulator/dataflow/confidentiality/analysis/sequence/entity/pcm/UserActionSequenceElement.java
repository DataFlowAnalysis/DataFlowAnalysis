package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.seff.SetVariableAction;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

public class UserActionSequenceElement<T extends AbstractUserAction> extends AbstractPCMActionSequenceElement<T> {

    public UserActionSequenceElement(T element) {
        super(element, new ArrayDeque<>());
    }

    public UserActionSequenceElement(UserActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
    }

    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
        if (this.getElement() instanceof StartAction) {
    		return new UserActionSequenceElement<T>(this, new ArrayList<>(variables), nodeCharacteristics);
    	} else if (!(this.getElement() instanceof SetVariableAction)) {
    		throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    	}
    	List<VariableCharacterisation> variableCharacterisations = ((SetVariableAction) this.getElement())
                .getLocalVariableUsages_SetVariableAction()
                .stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                    .stream())
                .toList();
    	CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables, nodeCharacteristics);
        variableCharacterisations.forEach(it -> characteristicsCalculator.evaluate(it));
        return new UserActionSequenceElement<T>(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeCharacteristics);
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
