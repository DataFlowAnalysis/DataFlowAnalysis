package org.dataflowanalysis.analysis.pcm.core.user;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.parameter.VariableUsage;
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
        List<DataCharacteristic> incomingDataCharacteristics = this.getIncomingDataCharacteristics();
        List<CharacteristicValue> nodeCharacteristics = this.getVertexCharacteristics();

        List<ConfidentialityVariableCharacterisation> variableCharacterisations = this.getVariableCharacterizations();

        if (this.isCalling()) {
            this.checkCallParameter(this.getReferencedElement().getOperationSignature__EntryLevelSystemCall(), variableCharacterisations);
        }

        List<DataCharacteristic> outgoingDataCharacteristics = this.getDataCharacteristics(nodeCharacteristics, variableCharacterisations,
                incomingDataCharacteristics);
        if (this.isReturning()) {
            outgoingDataCharacteristics = outgoingDataCharacteristics.stream()
                    .filter(it -> !it.getVariableName()
                            .equals("RETURN"))
                    .collect(Collectors.toList());
        }
        this.setPropagationResult(incomingDataCharacteristics, outgoingDataCharacteristics, nodeCharacteristics);
    }

    /**
     * Determines the variable characterizations that should be evaluated at the vertex. Calling User vertices evaluate
     * their input variable characterizations before calling. Returning User vertices evaluate their output variable
     * characterizations after returning from the called element
     * @return Returns a list of variable characterizations that are applicable to the current vertex
     */
    private List<ConfidentialityVariableCharacterisation> getVariableCharacterizations() {
        Stream<VariableUsage> relevantVariableUsages = this.isCalling ? super.getReferencedElement().getInputParameterUsages_EntryLevelSystemCall()
                .stream()
                : super.getReferencedElement().getOutputParameterUsages_EntryLevelSystemCall()
                        .stream();
        return relevantVariableUsages.flatMap(it -> it.getVariableCharacterisation_VariableUsage()
                .stream())
                .filter(ConfidentialityVariableCharacterisation.class::isInstance)
                .map(ConfidentialityVariableCharacterisation.class::cast)
                .collect(Collectors.toList());
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

    @Override
    public AbstractPCMVertex<?> copy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        if (vertexMapping.get(this) != null) {
            return vertexMapping.get(this);
        }
        CallingUserPCMVertex copy = new CallingUserPCMVertex(referencedElement, List.of(), isCalling, resourceProvider);
        return super.updateCopy(copy, vertexMapping);
    }

    @Override
    public boolean isEquivalentInContext(Object otherVertexObject) {
        if (!(otherVertexObject instanceof CallingUserPCMVertex otherVertex)) {
            return false;
        }
        return super.isEquivalentInContext(otherVertex) && this.isCalling() == otherVertex.isCalling();
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(this.getReferencedElement().getId(), this.isCalling);
    }
}
