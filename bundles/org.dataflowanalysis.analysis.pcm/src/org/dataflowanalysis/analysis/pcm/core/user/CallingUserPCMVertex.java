package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
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
        List<DataFlowVariable> incomingDataFlowVariables = List.of();
        if (!super.isSource()) {
            super.getPreviousElements().stream().filter(it -> !it.isEvaluated()).forEach(AbstractVertex::evaluateDataFlow);
            incomingDataFlowVariables = super.getPreviousElements().stream().flatMap(it -> it.getAllOutgoingDataFlowVariables().stream())
                    .collect(Collectors.toList());
        }

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

        List<DataFlowVariable> outgoingDataFlowVariables = super.getDataFlowVariables(nodeCharacteristics, variableCharacterisations,
                incomingDataFlowVariables);
        if (this.isReturning()) {
            outgoingDataFlowVariables = outgoingDataFlowVariables.stream().filter(it -> !it.getVariableName().equals("RETURN"))
                    .collect(Collectors.toList());
        }
        this.setPropagationResult(incomingDataFlowVariables, outgoingDataFlowVariables, nodeCharacteristics);
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
        if (this.isEvaluated()) {
            copy.setPropagationResult(this.getAllIncomingDataFlowVariables(), this.getAllOutgoingDataFlowVariables(),
                    this.getVertexCharacteristics());
        }
        isomorphism.put(this, copy);

        List<? extends AbstractPCMVertex<?>> clonedPreviousElements = this.previousElements.stream()
                .map(it -> it.deepCopy(isomorphism))
                .toList();

        copy.setPreviousElements(clonedPreviousElements);

        return copy;
    }
    
    @Override
    public boolean equals(Object otherVertexObject) {
        if (!(otherVertexObject instanceof CallingUserPCMVertex otherVertex)) {
            return false;
        }
        return super.equals(otherVertex) && this.isCalling() == otherVertex.isCalling();
    }
}
