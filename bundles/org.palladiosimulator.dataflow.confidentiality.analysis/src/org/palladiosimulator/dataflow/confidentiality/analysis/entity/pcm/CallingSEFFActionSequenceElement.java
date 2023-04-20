package org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.DataCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.PCMDataCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.DataFlowVariable;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFActionSequenceElement extends SEFFActionSequenceElement<ExternalCallAction>
        implements CallReturnBehavior {
    private final boolean isCalling;

    /**
     * Creates a new SEFF Action Sequence Element with an underlying Palladio Element, Assembly Context, List of present parameter and indication whether the SEFF Action is calling
     * @param element Underlying Palladio Element
     * @param context Assembly Context of the SEFF
     * @param parameter List of Parameters that are available for the calling SEFF
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingSEFFActionSequenceElement(ExternalCallAction element, Deque<AssemblyContext> context, List<Parameter> parameter, boolean isCalling) {
        super(element, context, parameter);
        this.isCalling = isCalling;
    }

    /**
     * Constructs a new SEFF Action Sequence element given an old element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old element, which attributes are copied
     * @param dataFlowVariables List of updated data flow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public CallingSEFFActionSequenceElement(CallingSEFFActionSequenceElement oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, nodeCharacteristics);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractActionSequenceElement<ExternalCallAction> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData) {
    	List<DataFlowVariable> newDataFlowVariables = new ArrayList<>(variables);
    	
    	List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics(analysisData);
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
        
        if (this.isCalling()) {
        	List<String> parameter = 
        			this.getElement().getCalledService_ExternalService()
        			.getParameters__OperationSignature().stream()
        			.map(it -> it.getParameterName())
        			.toList();
        	PCMDataCharacteristicsCalculator.checkParameter(this, parameter, variableCharacterisations);
        }

        DataCharacteristicsCalculator characteristicsCalculator = analysisData.getVariableCharacteristicsCalculator().createNodeCalculator(variables, nodeVariables);
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
