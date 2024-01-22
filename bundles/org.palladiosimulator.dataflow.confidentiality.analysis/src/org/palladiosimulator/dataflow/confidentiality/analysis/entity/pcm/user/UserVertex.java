package org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.user;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.AbstractPCMVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractVertex;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

public class UserVertex<T extends AbstractUserAction> extends AbstractPCMVertex<T> {
	private final Logger logger = Logger.getLogger(UserVertex.class);

	/**
	 * Creates a new User Sequence Element with the given Palladio User Action Element
	 * @param element
	 */
    public UserVertex(T element) {
        super(element, new ArrayDeque<>());
    }

    /**
     * Creates a new User Sequence Element using an old User Sequence Element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old User Sequence element, which attributes shall be copied
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public UserVertex(UserVertex<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public AbstractVertex<T> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData) {
    	List<CharacteristicValue> nodeCharacteristics = super.getNodeCharacteristics(analysisData);
        if (this.getElement() instanceof StartAction) {
    		return new UserVertex<T>(this, new ArrayList<>(variables), nodeCharacteristics);
    	} 
    	logger.error("Found unexpected sequence element of unknown PCM type " + this.getElement().getClass().getName());
    	throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
    }

}
