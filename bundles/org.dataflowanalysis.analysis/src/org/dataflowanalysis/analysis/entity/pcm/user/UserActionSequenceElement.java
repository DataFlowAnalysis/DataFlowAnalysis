package org.dataflowanalysis.analysis.entity.pcm.user;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.characteristics.CharacteristicValue;
import org.dataflowanalysis.analysis.characteristics.DataFlowVariable;
import org.dataflowanalysis.analysis.entity.pcm.AbstractPCMActionSequenceElement;
import org.dataflowanalysis.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class UserActionSequenceElement<T extends AbstractUserAction> extends AbstractPCMActionSequenceElement<T> {
	private final Logger logger = Logger.getLogger(UserActionSequenceElement.class);

	/**
	 * Creates a new User Sequence Element with the given Palladio User Action Element
	 * @param element
	 */
    public UserActionSequenceElement(T element) {
        super(element, new ArrayDeque<>());
    }

    /**
     * Creates a new User Sequence Element using an old User Sequence Element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old User Sequence element, which attributes shall be copied
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public UserActionSequenceElement(UserActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> incomingDataFlowVariables, AnalysisData analysisData) {
    	List<CharacteristicValue> nodeCharacteristics = super.getNodeCharacteristics(analysisData);
        if (this.getElement() instanceof Start || this.getElement() instanceof Stop) {
    		return new UserActionSequenceElement<T>(this, new ArrayList<>(incomingDataFlowVariables), new ArrayList<>(incomingDataFlowVariables), nodeCharacteristics);
    	} 
    	logger.error("Found unexpected sequence element of unknown PCM type " + this.getElement().getClass().getName());
    	throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    }

    @Override
    public String toString() {
    	if (this.getElement() instanceof Start) {
    		return String.format("%s (Starting %s, %s)", 
    				this.getClass().getSimpleName(), 
    				this.getElement().getScenarioBehaviour_AbstractUserAction().getUsageScenario_SenarioBehaviour().getEntityName(),
    				this.getElement().getId());
    	}
    	if (this.getElement() instanceof Stop) {
    		return String.format("%s (Stopping %s, %s)", 
    				this.getClass().getSimpleName(),
    				this.getElement().getScenarioBehaviour_AbstractUserAction().getUsageScenario_SenarioBehaviour().getEntityName(),
    				this.getElement().getId());
    	}
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
