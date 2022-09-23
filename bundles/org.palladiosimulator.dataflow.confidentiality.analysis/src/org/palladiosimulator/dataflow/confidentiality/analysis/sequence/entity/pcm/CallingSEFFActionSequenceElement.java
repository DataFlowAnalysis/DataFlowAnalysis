package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.expression.LhsEnumCharacteristicReference;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableUsage;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFActionSequenceElement extends SEFFActionSequenceElement<ExternalCallAction>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingSEFFActionSequenceElement(ExternalCallAction element, Deque<AssemblyContext> context,
            boolean isCalling) {
        super(element, context);
        this.isCalling = isCalling;
        // TODO Auto-generated constructor stub
    }
    
    public CallingSEFFActionSequenceElement(CallingSEFFActionSequenceElement oldElement, List<DataFlowVariable> variables) {
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
    public AbstractActionSequenceElement<ExternalCallAction> evaluateDataFlow(List<DataFlowVariable> variables) {
//    	var newVariables = new ArrayList<>(variables);
//    	var inputParameterUsage = super.getElement().getInputVariableUsages__CallAction();
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
//    				System.out.printf("%s.%s.%s modified at %s.%n", variable, characteristicValue, textValue, this.getElement().getEntityName());
//    			} else {
//    				// Variable does not exist in scope
//    				DataFlowVariable newVariable = new DataFlowVariable(variable);
//    				newVariable.addCharacteristic(new CharacteristicValue(characteristicType, value));
//    				newVariables.add(newVariable);
//    				var characteristicValue = characteristicType != null ? characteristicType.getName() : "*";
//    				var textValue = value != null ? value.getName() : "*";
//    				System.out.printf("%s.%s.%s created at %s.%n", variable, characteristicValue, textValue, this.getElement().getEntityName());
//    			}
//    			
//    		}
//    	}
    	
    	
    	var newVariables = new ArrayList<>(variables);
    	super.getElement().getInputVariableUsages__CallAction().stream()
	    	.flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
	    	.map(ConfidentialityVariableCharacterisation.class::cast)
	    	.forEach(it -> evaluateVariableCharacterisation(it, variables, newVariables));
    	
    	AbstractActionSequenceElement<ExternalCallAction> evaluatedElement = new CallingSEFFActionSequenceElement(this, newVariables);
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
