package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.ArrayDeque;
import java.util.List;
import java.util.stream.Collectors;

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
    public UserPCMVertex(T element, List<? extends AbstractVertex<?>> previousElements, ResourceProvider resourceProvider) {
        super(element, previousElements, new ArrayDeque<>(), resourceProvider);
    }
    
    @Override
    public void evaluateDataFlow() {
    	List<DataFlowVariable> incomingDataFlowVariables = List.of();
    	if(!super.isSource()) {
    		super.getPreviousElements().stream()
    			.filter(it -> !it.isEvaluated())
    			.forEach(AbstractVertex::evaluateDataFlow);
        	incomingDataFlowVariables = super.getPreviousElements().stream()
        			.flatMap(it -> it.getAllOutgoingDataFlowVariables().stream())
        			.collect(Collectors.toList());
    	}
    	
    	List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics();
        if (this.getReferencedElement() instanceof Start || this.getReferencedElement() instanceof Stop) {
        	this.setPropagationResult(incomingDataFlowVariables, incomingDataFlowVariables, nodeCharacteristics);
        	return;
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
