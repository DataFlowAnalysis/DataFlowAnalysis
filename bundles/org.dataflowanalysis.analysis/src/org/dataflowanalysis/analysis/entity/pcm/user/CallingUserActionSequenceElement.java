package org.dataflowanalysis.analysis.entity.pcm.user;

import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.characteristics.CharacteristicValue;
import org.dataflowanalysis.analysis.characteristics.DataFlowVariable;
import org.dataflowanalysis.analysis.entity.CallReturnBehavior;
import org.dataflowanalysis.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.pcm.parameter.VariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserActionSequenceElement extends UserActionSequenceElement<EntryLevelSystemCall>
        implements CallReturnBehavior {
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
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData) {
    	List<CharacteristicValue> nodeCharacteristics = super.getNodeCharacteristics(analysisData);
    	
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
        	super.checkCallParameter(super.getElement().getOperationSignature__EntryLevelSystemCall(), variableCharacterisations);
        }
    	
    	List<DataFlowVariable> dataFlowVariables = super.getDataFlowVariables(analysisData, nodeCharacteristics, variableCharacterisations, variables);
    	return new CallingUserActionSequenceElement(this, dataFlowVariables, nodeCharacteristics);
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
