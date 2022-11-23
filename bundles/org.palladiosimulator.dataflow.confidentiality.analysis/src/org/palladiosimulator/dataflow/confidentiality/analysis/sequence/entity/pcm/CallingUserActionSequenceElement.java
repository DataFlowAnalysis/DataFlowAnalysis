package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMQueryUtils;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.characteristics.EnumCharacteristic;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.profile.ProfileConstants;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.palladiosimulator.mdsdprofiles.api.StereotypeAPI;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;

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

    // TODO: Custom hash and equals required?

    /**
     * Input: ccd . GrantedRoles . User := true Elements: variable.characteristicType.value := Term
     */
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(Deque<List<DataFlowVariable>> variables) {
    	List<DataFlowVariable> newDataFlowVariables = variables.getLast();
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
        if (!this.isCalling()) {
        	
        }
        return evaluatedElement;
    }
    
    @Override
    public List<DataFlowVariable> getAvailableDataFlowVariables(List<DataFlowVariable> variables) {
    	List<String> availableVariableNames = this.getParameter().stream()
    			.map(it -> it.getParameterName())
    			.collect(Collectors.toList());
    	if (!this.isCalling()) {
    		availableVariableNames.add("RETURN");
    	}
    	return variables.stream()
    			.filter(it -> availableVariableNames.contains(it.variableName()))
    			.collect(Collectors.toList());
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
