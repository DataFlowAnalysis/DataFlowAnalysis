package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingUserActionSequenceElement(EntryLevelSystemCall element, boolean isCalling) {
        super(element);
        this.isCalling = isCalling;
    }

    public CallingUserActionSequenceElement(CallingUserActionSequenceElement oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<DataFlowVariable> newDataFlowVariables;
    	if (this.isCalling()) {
    		newDataFlowVariables = new ArrayList<>(variables);
    	} else {
    		newDataFlowVariables = variables.stream()
				.filter(it -> it.variableName().equals("RETURN"))
				.collect(Collectors.toList());
    	}
    			
    	List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
    	
    	List<VariableCharacterisation> variableCharacterisations = this.isCalling ?
    			super.getElement().getInputParameterUsages_EntryLevelSystemCall().stream()
    			.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
    	                .stream())
    	            .collect(Collectors.toList())
                :
                super.getElement().getOutputParameterUsages_EntryLevelSystemCall().stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
                    .collect(Collectors.toList());

        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(newDataFlowVariables, nodeVariables);
        variableCharacterisations.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
        AbstractActionSequenceElement<EntryLevelSystemCall> evaluatedElement = new CallingUserActionSequenceElement(
                this, characteristicsCalculator.getCalculatedCharacteristics(), nodeVariables);
        return evaluatedElement;
    }

    @Override
    public String toString() {
        String calling = isCalling ? "calling" : "returning";
        return String.format("%s / %s (%s, %s))", this.getClass()
            .getSimpleName(), calling,
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
