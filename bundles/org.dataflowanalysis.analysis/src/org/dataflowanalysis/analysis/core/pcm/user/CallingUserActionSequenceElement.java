package org.dataflowanalysis.analysis.core.pcm.user;

import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.pcm.CallReturnBehavior;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
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
    public CallingUserActionSequenceElement(CallingUserActionSequenceElement oldElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractActionSequenceElement<EntryLevelSystemCall> evaluateDataFlow(List<DataFlowVariable> incomingDataFlowVariables, AnalysisData analysisData) {
    	List<CharacteristicValue> nodeCharacteristics = super.getNodeCharacteristics(analysisData);
    	
    	List<ConfidentialityVariableCharacterisation> variableCharacterisations = this.isCalling ?
    			super.getElement().getInputParameterUsages_EntryLevelSystemCall().stream()
    			.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
    	        .stream())
    			.filter(ConfidentialityVariableCharacterisation.class::isInstance)
    			.map(ConfidentialityVariableCharacterisation.class::cast)
    	            .collect(Collectors.toList())
                :
                super.getElement().getOutputParameterUsages_EntryLevelSystemCall().stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
                .filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                    .collect(Collectors.toList());
    	
    	if (this.isCalling()) {
        	super.checkCallParameter(super.getElement().getOperationSignature__EntryLevelSystemCall(), variableCharacterisations);
        }
    	
    	List<DataFlowVariable> outgoingDataFlowVariables = super.getDataFlowVariables(analysisData, nodeCharacteristics, variableCharacterisations, incomingDataFlowVariables);
    	return new CallingUserActionSequenceElement(this, incomingDataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
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
