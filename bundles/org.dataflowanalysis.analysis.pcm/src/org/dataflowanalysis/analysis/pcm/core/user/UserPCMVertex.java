package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;

public class UserPCMVertex<T extends AbstractUserAction> extends AbstractPCMVertex<T> {
	private final Logger logger = Logger.getLogger(UserPCMVertex.class);
	
	/**
	 * Creates a new User Sequence Element with the given Palladio User Action Element
	 * @param element
	 */
    public UserPCMVertex(T element, ResourceProvider resourceProvider) {
        super(element, new ArrayDeque<>(), resourceProvider);
    }

	/**
	 * Creates a new User Sequence Element with the given Palladio User Action Element
	 * @param element
	 */
    public UserPCMVertex(T element, AbstractPCMVertex<?> previousElement, ResourceProvider resourceProvider) {
        super(element, previousElement, new ArrayDeque<>(), resourceProvider);
    }
    
    /**
     * Creates a new User Sequence Element using an old User Sequence Element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old User Sequence element, which attributes shall be copied
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public UserPCMVertex(UserPCMVertex<T> oldElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
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
    public AbstractVertex<T> evaluateDataFlow() {
    	AbstractVertex<?> previousVertex = null;
    	List<DataFlowVariable> incomingDataFlowVariables = List.of();
    	if(!super.isSource()) {
        	previousVertex = super.getPreviousVertex().evaluateDataFlow();
        	incomingDataFlowVariables = previousVertex.getAllOutgoingDataFlowVariables();
    	}
    	
    	List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics();
        if (this.getReferencedElement() instanceof Start || this.getReferencedElement() instanceof Stop) {
        	if (previousVertex == null) {
        		return new UserPCMVertex<T>(this, new ArrayList<>(incomingDataFlowVariables), new ArrayList<>(incomingDataFlowVariables), nodeCharacteristics);
        	}
    		return new UserPCMVertex<T>(this, previousVertex, new ArrayList<>(incomingDataFlowVariables), new ArrayList<>(incomingDataFlowVariables), nodeCharacteristics);
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
