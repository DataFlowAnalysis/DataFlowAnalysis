package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.usagemodel.EntryLevelSystemCall;

public class CallingUserPCMVertex extends UserPCMVertex<EntryLevelSystemCall> implements CallReturnBehavior {
    private final boolean isCalling;

    /**
     * Creates a new User Action Sequence Element with an underlying Palladio Element and indication whether the SEFF Action
     * is calling
     * @param element Underlying Palladio Element
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingUserPCMVertex(EntryLevelSystemCall element, List<? extends AbstractPCMVertex<?>> previousElements, boolean isCalling,
            ResourceProvider resourceProvider) {
        super(element, previousElements, resourceProvider);
        this.isCalling = isCalling;
    }

    @Override
    public boolean isCalling() {
        return this.isCalling;
    }

    @Override
    public void evaluateDataFlow() {
        List<DataFlowVariable> incomingDataFlowVariables = getIncomingDataFlowVariables();
        List<CharacteristicValue> nodeCharacteristics = super.getVertexCharacteristics();

        List<ConfidentialityVariableCharacterisation> variableCharacterisations = this.isCalling
                ? super.getReferencedElement().getInputParameterUsages_EntryLevelSystemCall().stream()
                        .flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
                        .filter(ConfidentialityVariableCharacterisation.class::isInstance).map(ConfidentialityVariableCharacterisation.class::cast)
                        .collect(Collectors.toList())
                : super.getReferencedElement().getOutputParameterUsages_EntryLevelSystemCall().stream()
                        .flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
                        .filter(ConfidentialityVariableCharacterisation.class::isInstance).map(ConfidentialityVariableCharacterisation.class::cast)
                        .collect(Collectors.toList());

        if (this.isCalling()) {
            super.checkCallParameter(super.getReferencedElement().getOperationSignature__EntryLevelSystemCall(), variableCharacterisations);
        }

        List<DataFlowVariable> outgoingDataFlowVariables = getDataFlowVariables(nodeCharacteristics, variableCharacterisations,
                incomingDataFlowVariables);
        if (this.isReturning()) {
            outgoingDataFlowVariables = removeReturnParameter(outgoingDataFlowVariables);
        }
        this.setPropagationResult(incomingDataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
    }
    
    /**
	 * Modifies the outgoing DataFlowVariables to not contain RETURN.
	 * 
	 * @param outgoingDataFlowVariables the outgoing DataFlowVariables to be
	 *                                  modified
	 * @return the filtered outgoing DataFlowVariables
	 */
    protected List<DataFlowVariable> removeReturnParameter(List<DataFlowVariable> outgoingDataFlowVariables) {
    	return outgoingDataFlowVariables.stream().filter(it -> !it.getVariableName().equals("RETURN"))
        .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String calling = isCalling ? "calling" : "returning";
        return String.format("%s / %s (%s, %s))", this.getClass().getSimpleName(), calling, this.getReferencedElement().getEntityName(),
                this.getReferencedElement().getId());
    }

    @Override
    public AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
        if (isomorphism.get(this) != null) {
            return isomorphism.get(this);
        }
        CallingUserPCMVertex copy = new CallingUserPCMVertex(referencedElement, List.of(), isCalling, resourceProvider);
        return super.updateCopy(copy, isomorphism);
    }

    @Override
    public boolean equals(Object otherVertexObject) {
        if (!(otherVertexObject instanceof CallingUserPCMVertex otherVertex)) {
            return false;
        }
        return super.equals(otherVertex) && this.isCalling() == otherVertex.isCalling();
    }
}
