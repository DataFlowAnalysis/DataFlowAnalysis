package org.dataflowanalysis.analysis.pcm.core.seff;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.CallReturnBehavior;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.Parameter;
import org.palladiosimulator.pcm.seff.ExternalCallAction;

public class CallingSEFFPCMVertex extends SEFFPCMVertex<ExternalCallAction> implements CallReturnBehavior {
    private final boolean isCalling;

    /**
     * Creates a new SEFF Action Sequence Element with an underlying Palladio Element, Assembly Context, List of present
     * parameter and indication whether the SEFF Action is calling
     * @param element Underlying Palladio Element
     * @param context Assembly Context of the SEFF
     * @param parameter List of Parameters that are available for the calling SEFF
     * @param isCalling Is true, when another method is called. Otherwise, a called method is returned from
     */
    public CallingSEFFPCMVertex(ExternalCallAction element, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
            List<Parameter> parameter, boolean isCalling, ResourceProvider resourceProvider) {
        super(element, previousElements, context, parameter, resourceProvider);
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
                ? super.getReferencedElement().getInputVariableUsages__CallAction().stream()
                        .flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
                        .filter(ConfidentialityVariableCharacterisation.class::isInstance).map(ConfidentialityVariableCharacterisation.class::cast)
                        .collect(Collectors.toList())
                : super.getReferencedElement().getReturnVariableUsage__CallReturnAction().stream()
                        .flatMap(it -> it.getVariableCharacterisation_VariableUsage().stream())
                        .filter(ConfidentialityVariableCharacterisation.class::isInstance).map(ConfidentialityVariableCharacterisation.class::cast)
                        .collect(Collectors.toList());
        if (this.isCalling()) {
            super.checkCallParameter(super.getReferencedElement().getCalledService_ExternalService(), variableCharacterisations);
        }
        List<DataFlowVariable> outgoingDataFlowVariables = getDataFlowVariables(nodeCharacteristics, variableCharacterisations,
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
        CallingSEFFPCMVertex copy = new CallingSEFFPCMVertex(referencedElement, List.of(), new ArrayDeque<>(context),
                new ArrayList<>(this.getParameter()), isCalling, resourceProvider);
        return super.updateCopy(copy, isomorphism);
    }

    @Override
    public boolean equals(Object otherVertexObject) {
        if (!(otherVertexObject instanceof CallingSEFFPCMVertex otherVertex)) {
            return false;
        }
        return super.equals(otherVertex) && this.isCalling() == otherVertex.isCalling();
    }
}
