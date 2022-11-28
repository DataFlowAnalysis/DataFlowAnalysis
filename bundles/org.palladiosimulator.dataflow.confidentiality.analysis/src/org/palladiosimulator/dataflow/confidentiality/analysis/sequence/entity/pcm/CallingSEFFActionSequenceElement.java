package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.CharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFActionSequenceElement extends SEFFActionSequenceElement<ExternalCallAction>
        implements CallReturnBehavior {

    private final boolean isCalling;

    public CallingSEFFActionSequenceElement(ExternalCallAction element, Deque<AssemblyContext> context, List<Parameter> parameter, boolean isCalling) {
        super(element, context, parameter);
        this.isCalling = isCalling;
    }

    public CallingSEFFActionSequenceElement(CallingSEFFActionSequenceElement oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeVariables) {
        super(oldElement, dataFlowVariables, nodeVariables);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractActionSequenceElement<ExternalCallAction> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<DataFlowVariable> newDataFlowVariables;
    	if (this.isCalling()) {
    		newDataFlowVariables = new ArrayList<>(variables);
    	} else {
    		newDataFlowVariables = variables.stream()
    				.filter(it -> it.variableName().equals("RETURN"))
    				.collect(Collectors.toList());;
    	}
    	
    	List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
        List<VariableCharacterisation> variableCharacterisations = this.isCalling ? 
        		super.getElement().getInputVariableUsages__CallAction().stream()
        		.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
                    .collect(Collectors.toList())
                : 
                super.getElement().getReturnVariableUsage__CallReturnAction().stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
                .collect(Collectors.toList());

        CharacteristicsCalculator characteristicsCalculator = new CharacteristicsCalculator(newDataFlowVariables, nodeVariables);
        variableCharacterisations.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
        AbstractActionSequenceElement<ExternalCallAction> evaluatedElement = new CallingSEFFActionSequenceElement(this,
                characteristicsCalculator.getCalculatedCharacteristics(), nodeVariables);
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
