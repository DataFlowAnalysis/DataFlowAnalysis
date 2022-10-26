package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.seff.SetVariableAction;
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
    	List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
        List<VariableCharacterisation> dataflowElements = ((SetVariableAction) super.getElement())
            .getLocalVariableUsages_SetVariableAction()
            .stream()
            .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
            .toList();
        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(variables, nodeVariables);
        dataflowElements.forEach(it -> characteristicsCalculator.evaluate(it));
        return new UserActionSequenceElement<T>(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeVariables);
    }
    
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	List<CharacteristicValue> nodeVariables = new ArrayList<>();
    	var usageScenario = PCMQueryUtils.findParentOfType(this.getElement(), UsageScenario.class, false).get();
    	var nodeCharacteristics = StereotypeAPI.<List<EnumCharacteristic>>getTaggedValueSafe(usageScenario, ProfileConstants.characterisable.getValue(), ProfileConstants.characterisable.getStereotype());
    	if (nodeCharacteristics.isPresent()) {
    		var nodeEnumCharacteristics = nodeCharacteristics.get();
    		for (EnumCharacteristic nodeEnumCharacteristic : nodeEnumCharacteristics) {
        		for (Literal nodeLiteral : nodeEnumCharacteristic.getValues()) {
        			nodeVariables.add(new CharacteristicValue(nodeEnumCharacteristic.getType(), nodeLiteral));
        		}
    		}
    	}
    	return nodeVariables;
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
