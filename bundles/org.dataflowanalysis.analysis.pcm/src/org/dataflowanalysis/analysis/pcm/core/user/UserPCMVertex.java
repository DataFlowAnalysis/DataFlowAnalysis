package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.VertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class UserPCMVertex<T extends AbstractUserAction> extends AbstractPCMVertex<T> {
	private final Logger logger = Logger.getLogger(UserPCMVertex.class);

	/**
	 * Creates a new User Sequence Element with the given Palladio User Action Element
	 * @param element
	 */
    public UserPCMVertex(T element, AbstractPCMVertex<?> previousElement) {
        super(element, previousElement, new ArrayDeque<>());
    }

    /**
     * Creates a new User Sequence Element using an old User Sequence Element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old User Sequence element, which attributes shall be copied
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public UserPCMVertex(UserPCMVertex<T> oldElement, AbstractVertex<?> previousElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, previousElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
    }
    
    @Override
    public AbstractVertex<T> evaluateDataFlow(AbstractVertex<?> previousElement, List<DataFlowVariable> incomingDataFlowVariables, 
    		VertexCharacteristicsCalculator nodeCharacteristicsCalculator, DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
    	List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics(nodeCharacteristicsCalculator);
        if (this.getReferencedElement() instanceof Start || this.getReferencedElement() instanceof Stop) {
    		return new UserPCMVertex<T>(this, previousElement, new ArrayList<>(incomingDataFlowVariables), new ArrayList<>(incomingDataFlowVariables), nodeCharacteristics);
    	} 
    	logger.error("Found unexpected sequence element of unknown PCM type " + this.getReferencedElement().getClass().getName());
    	throw new IllegalStateException("Unexpected action sequence element with unknown PCM type");
    }

    @Override
    public String toString() {
    	if (this.getReferencedElement() instanceof Start) {
    		return String.format("%s (Starting %s, %s)", 
    				this.getClass().getSimpleName(), 
    				this.getEntityNameOfScenarioBehaviour(),
    				this.getReferencedElement().getId());
    	}
    	if (this.getReferencedElement() instanceof Stop) {
    		return String.format("%s (Stopping %s, %s)", 
    				this.getClass().getSimpleName(),
    				this.getEntityNameOfScenarioBehaviour(),
    				this.getReferencedElement().getId());
    	}
        return String.format("%s (%s, %s))", this.getClass()
            .getSimpleName(),
                this.getReferencedElement()
                    .getEntityName(),
                this.getReferencedElement()
                    .getId());
    }
    
    private String getEntityNameOfScenarioBehaviour() {
    	if(this.getReferencedElement().getScenarioBehaviour_AbstractUserAction().getUsageScenario_SenarioBehaviour() != null) {
    		return "usage: %s".formatted(this.getReferencedElement().getScenarioBehaviour_AbstractUserAction().getUsageScenario_SenarioBehaviour().getEntityName());
    	} else {
    		return "branch: %s".formatted(this.getReferencedElement().getScenarioBehaviour_AbstractUserAction().getBranchTransition_ScenarioBehaviour().getBranch_BranchTransition().getEntityName());
    	}
    }

}
