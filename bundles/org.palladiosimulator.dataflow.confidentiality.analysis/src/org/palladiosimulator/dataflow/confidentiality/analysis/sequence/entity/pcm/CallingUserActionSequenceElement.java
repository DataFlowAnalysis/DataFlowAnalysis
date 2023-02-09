package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMDataCharacteristicsCalculator;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {
	private final Logger logger = Logger.getLogger(CallingUserActionSequenceElement.class);

    private final boolean isCalling;

    /**
     * Creates a new User Action Sequence Element with an underlying Palladio Element and indication whether the SEFF Action is calling
     * @param element Underlying Palladio Element
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingUserActionSequenceElement(EntryLevelSystemCall element, boolean isCalling) {
        super(element);
        this.isCalling = isCalling;
    }

    /**
     * Constructs a new User Action Sequence element given an old element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old element, which attributes are copied
     * @param dataFlowVariables List of updated data flow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public CallingUserActionSequenceElement(CallingUserActionSequenceElement oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, nodeCharacteristics);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> variables) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics();
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
    	
    	if (this.isCalling()) {
        	checkParameter(variableCharacterisations);
        }
    	

        PCMDataCharacteristicsCalculator characteristicsCalculator = new PCMDataCharacteristicsCalculator(new ArrayList<>(variables), nodeCharacteristics);
        variableCharacterisations.stream()
            .forEach(it -> characteristicsCalculator.evaluate(it));
       return new CallingUserActionSequenceElement(this, characteristicsCalculator.getCalculatedCharacteristics(), nodeCharacteristics);
    }
    
    private void checkParameter(List<VariableCharacterisation> variableCharacterisations) {
    	List<String> parameter = 
    			this.getElement().getOperationSignature__EntryLevelSystemCall().getParameters__OperationSignature().stream()
    			.map(it -> it.getParameterName())
    			.toList();
    	
    	List<String> referencedParameter =
    			variableCharacterisations.stream()
    			.map(it -> it.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage().getReferenceName())
    			.toList();
    	
    	referencedParameter.stream()
    	.filter(it -> !parameter.contains(it))
    	.forEach(it -> {
    		logger.warn("Unknown reference to variable " + it + " in variable characterisation in element " + this.toString());
    		logger.warn("Present variables:" + parameter + ", Referenced parameter: " + referencedParameter);
    	});
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
