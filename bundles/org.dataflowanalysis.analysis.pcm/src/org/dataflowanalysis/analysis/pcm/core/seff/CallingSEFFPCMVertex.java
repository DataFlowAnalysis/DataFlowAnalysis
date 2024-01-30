package org.dataflowanalysis.analysis.pcm.core.seff;

import java.util.Deque;
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
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFPCMVertex extends SEFFPCMVertex<ExternalCallAction>
        implements CallReturnBehavior {
    private final boolean isCalling;

    /**
     * Creates a new SEFF Action Sequence Element with an underlying Palladio Element, Assembly Context, List of present parameter and indication whether the SEFF Action is calling
     * @param element Underlying Palladio Element
     * @param context Assembly Context of the SEFF
     * @param parameter List of Parameters that are available for the calling SEFF
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingSEFFPCMVertex(ExternalCallAction element, AbstractPCMVertex<?> previousElement, Deque<AssemblyContext> context, List<Parameter> parameter, boolean isCalling) {
        super(element, previousElement, context, parameter);
        this.isCalling = isCalling;
    }

    /**
     * Constructs a new SEFF Action Sequence element given an old element and a list of updated dataflow variables and node characteristics
     * @param oldElement Old element, which attributes are copied
     * @param dataFlowVariables List of updated data flow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public CallingSEFFPCMVertex(CallingSEFFPCMVertex oldElement, AbstractVertex<?> previousElement, List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        super(oldElement, previousElement, dataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
        this.isCalling = oldElement.isCalling();
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }
    
    @Override
    public AbstractVertex<ExternalCallAction> evaluateDataFlow(AbstractVertex<?> previousVertex, List<DataFlowVariable> incomingDataFlowVariables, 
    		VertexCharacteristicsCalculator nodeCharacteristicsCalculator, DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
    	List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics(nodeCharacteristicsCalculator);
    	
        List<ConfidentialityVariableCharacterisation> variableCharacterisations = this.isCalling ? 
        		super.getReferencedElement().getInputVariableUsages__CallAction().stream()
        		.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
        		.filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                    .collect(Collectors.toList())
                : 
                super.getReferencedElement().getReturnVariableUsage__CallReturnAction().stream()
                .flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                        .stream())
                .filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                .collect(Collectors.toList());
        if (this.isCalling()) {
        	super.checkCallParameter(super.getReferencedElement().getCalledService_ExternalService(), variableCharacterisations);
        }

        List<DataFlowVariable> outgoingDataFlowVariables = super.getDataFlowVariables(dataCharacteristicsCalculatorFactory, nodeCharacteristics, variableCharacterisations, incomingDataFlowVariables);
        return new CallingSEFFPCMVertex(this, previousVertex, incomingDataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
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
