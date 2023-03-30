package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMNodeCharacteristicsCalculator;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

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
    public UserActionSequenceElement(UserActionSequenceElement<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables, PCMResourceLoader resourceLoader) {
    	List<CharacteristicValue> nodeCharacteristics = this.evaluateNodeCharacteristics(resourceLoader);
        if (this.getElement() instanceof StartAction) {
    		return new UserActionSequenceElement<T>(this, new ArrayList<>(variables), nodeCharacteristics);
    	} 
    	logger.error("Found unexpected sequence element of unknown PCM type " + this.getElement().getClass().getName());
    	throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    }
    
    /**
     * Calculates the node characteristics for an {@link UserActionSequenceElement} using the {@link PCMNodeCharacteristicsCalculator}
     * @return List of CharacteristicValues which are present at the current node
     */
    protected List<CharacteristicValue> evaluateNodeCharacteristics(PCMResourceLoader resourceLoader) {
    	PCMNodeCharacteristicsCalculator characteristicsCalculator = new PCMNodeCharacteristicsCalculator(this.getElement(), resourceLoader);
    	return characteristicsCalculator.getNodeCharacteristics(Optional.empty());
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
