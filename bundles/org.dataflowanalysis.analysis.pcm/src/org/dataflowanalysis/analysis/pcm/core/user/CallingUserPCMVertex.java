package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.VertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserPCMVertex extends UserPCMVertex<EntryLevelSystemCall>
        implements CallReturnBehavior {
    private final boolean isCalling;

    /**
     * Creates a new User Action Sequence Element with an underlying Palladio Element and indication whether the SEFF Action is calling
     * @param element Underlying Palladio Element
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingUserPCMVertex(EntryLevelSystemCall element, AbstractPCMVertex<?> previousElement, boolean isCalling) {
        super(element, previousElement);
        this.isCalling = isCalling;
    }

    /**
     * Constructs a new User Action Sequence element given an old element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old element, which attributes are copied
     * @param dataFlowVariables List of updated data flow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public CallingUserPCMVertex(CallingUserPCMVertex oldElement,  AbstractVertex<?> previousElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, previousElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractVertex<EntryLevelSystemCall> evaluateDataFlow(AbstractVertex<?> previousElement, List<DataFlowVariable> incomingDataFlowVariables, 
    		VertexCharacteristicsCalculator nodeCharacteristicsCalculator, DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
    	List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics(nodeCharacteristicsCalculator);
    	
    	List<ConfidentialityVariableCharacterisation> variableCharacterisations = this.isCalling ?
    			super.getReferencedElement().getInputParameterUsages_EntryLevelSystemCall().stream()
    			.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
    	        .stream())
    			.filter(ConfidentialityVariableCharacterisation.class::isInstance)
    			.map(ConfidentialityVariableCharacterisation.class::cast)
    	            .collect(Collectors.toList())
                :
                super.getReferencedElement().getOutputParameterUsages_EntryLevelSystemCall().stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
                .filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                    .collect(Collectors.toList());
    	
    	if (this.isCalling()) {
        	super.checkCallParameter(super.getReferencedElement().getOperationSignature__EntryLevelSystemCall(), variableCharacterisations);
        }
    	
    	List<DataFlowVariable> outgoingDataFlowVariables = super.getDataFlowVariables(dataCharacteristicsCalculatorFactory, nodeCharacteristics, variableCharacterisations, incomingDataFlowVariables);
    	return new CallingUserPCMVertex(this, previousElement, incomingDataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
    }

    @Override
    public String toString() {
        String calling = isCalling ? "calling" : "returning";
        return String.format("%s / %s (%s, %s))", this.getClass()
            .getSimpleName(), calling,
                this.getReferencedElement()
                    .getEntityName(),
                this.getReferencedElement()
                    .getId());
    }

}
