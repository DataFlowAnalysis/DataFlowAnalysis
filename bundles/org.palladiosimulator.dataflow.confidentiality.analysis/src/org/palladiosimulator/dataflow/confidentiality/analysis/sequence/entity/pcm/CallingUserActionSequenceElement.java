package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.seff.ExternalCallAction;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingUserActionSequenceElement(EntryLevelSystemCall element, boolean isCalling) {
        super(element);
        this.isCalling = isCalling;
        // TODO Auto-generated constructor stub
    }
    
    public CallingUserActionSequenceElement(CallingUserActionSequenceElement oldElement, List<DataFlowVariable> variables) {
    	super(oldElement, variables);
    	this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }

    // TODO: Custom hash and equals required?

    /**
     * Input:       ccd  .    GrantedRoles  . User := true
     * Elements: variable.characteristicType.value := Term
     */
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> variables) {
//    	List<DataFlowVariable> newVariables = new ArrayList<>(variables);
//    	var inputParameterUsage = super.getElement().getInputParameterUsages_EntryLevelSystemCall();
//    	for (VariableUsage parameterUsage : inputParameterUsage) {
//    		var variableCharacterisations = parameterUsage.getVariableCharacterisation_VariableUsage();
//    		for (VariableCharacterisation variableCharacterisation : variableCharacterisations) {
//    			if (!(variableCharacterisation instanceof ConfidentialityVariableCharacterisation)) {
//    				continue;
//    			}
//    			var confidentialityVariable = (ConfidentialityVariableCharacterisation) variableCharacterisation;
//    			var lhs = (LhsEnumCharacteristicReference) confidentialityVariable.getLhs();
//    			// var newValue = confidentialityVariable.getRhs(); // Can be True, Or, etc.
//    			
//    			var variable = parameterUsage.getNamedReference__VariableUsage().getReferenceName();
//    			var characteristicType = (EnumCharacteristicType) lhs.getCharacteristicType();
//    			var value = lhs.getLiteral();
//    			
//    			if (variables.stream().anyMatch(it -> it.variableName().equals(variable))) {
//    				// Variable exists in scope
//    				var characteristicValue = characteristicType != null ? characteristicType.getName() : "*";
//    				var textValue = value != null ? value.getName() : "*";
//    				//System.out.printf("%s.%s.%s modified at %s.%n", variable, characteristicValue, textValue, this.getElement().getEntityName());
//    			} else {
//    				// Variable does not exist in scope
//    				DataFlowVariable newVariable = new DataFlowVariable(variable);
//    				newVariable.addCharacteristic(new CharacteristicValue(characteristicType, value));
//    				newVariables.add(newVariable);
//    				var characteristicValue = characteristicType != null ? characteristicType.getName() : "*";
//    				var textValue = value != null ? value.getName() : "*";
//    				//System.out.printf("%s.%s.%s created at %s.%n", variable, characteristicValue, textValue, this.getElement().getEntityName());
//    			}
//    			
//    		}
//   	}
    	
    	var newVariables = new ArrayList<>(variables);
    	super.getElement().getInputParameterUsages_EntryLevelSystemCall().stream()
	    	.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
	    	.map(ConfidentialityVariableCharacterisation.class::cast)
	    	.forEach(it -> evaluateVariableCharacterisation(it, variables, newVariables));
    	
    	AbstractActionSequenceElement<EntryLevelSystemCall> evaluatedElement = new CallingUserActionSequenceElement(this, newVariables);
       return evaluatedElement;
    }
    
    private void evaluateVariableCharacterisation(ConfidentialityVariableCharacterisation confidentialityVariable, List<DataFlowVariable> oldVariables, List<DataFlowVariable> newVariables) {
    	var rhs = confidentialityVariable.getRhs();
    	CharacteristicsCalculator.evaluateLhs(confidentialityVariable, "Placeholder", oldVariables, newVariables);
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
