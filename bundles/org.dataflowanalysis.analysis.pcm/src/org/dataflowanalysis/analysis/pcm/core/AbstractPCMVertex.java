package org.dataflowanalysis.analysis.pcm.core;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.Parameter;

public abstract class AbstractPCMVertex<T extends Entity> extends AbstractVertex<T> {
    private final Logger logger = Logger.getLogger(AbstractPCMVertex.class);

    protected final Deque<AssemblyContext> context;
    protected final ResourceProvider resourceProvider;
    protected List<? extends AbstractPCMVertex<?>> previousElements;

    /**
     * Constructs a new abstract pcm vertex with the underlying palladio element and assembly context
     * @param referencedElement Palladio element that is referenced by the vertex
     * @param context Assembly context of the vertex
     * @param resourceProvider Resource provider of the vertex used to calculate vertex and data characteristics
     */
    public AbstractPCMVertex(T referencedElement, Deque<AssemblyContext> context, ResourceProvider resourceProvider) {
        super(referencedElement);
        this.context = context;
        this.resourceProvider = resourceProvider;
        this.previousElements = List.of();
    }

    /**
     * Constructs a new abstract pcm vertex with the underlying palladio element and assembly context
     * @param referencedElement Palladio element that is referenced by the vertex
     * @param previousElements List of vertices that preceded the vertex
     * @param context Assembly context of the vertex
     * @param resourceProvider Resource provider of the vertex used to calculate vertex and data characteristics
     */
    public AbstractPCMVertex(T referencedElement, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
            ResourceProvider resourceProvider) {
        super(referencedElement);
        this.context = context;
        this.resourceProvider = resourceProvider;
        this.previousElements = previousElements;
    }

    public abstract AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping);

    /**
     * Sets the propagation result of the Vertex to the given result. This method should only be called once on elements
     * that are not evaluated.
     * @param incomingDataFlowVariables Incoming data flow variables that flow into the vertex
     * @param outgoingDataFlowVariables Outgoing data flow variables that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    @Override
    protected void setPropagationResult(List<DataFlowVariable> incomingDataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables,
            List<CharacteristicValue> vertexCharacteristics) {
        super.setPropagationResult(incomingDataFlowVariables, outgoingDataFlowVariables, vertexCharacteristics);
    }

    @Override
    public List<? extends AbstractVertex<?>> getPreviousElements() {
        return this.previousElements;
    }

    public void setPreviousElements(List<? extends AbstractPCMVertex<?>> previousElements) {
        this.previousElements = previousElements;
    }

    protected List<DataFlowVariable> getIncomingDataFlowVariables() {
        if (super.isSource())
            return List.of();

        this.getPreviousElements().stream().filter(it -> !it.isEvaluated()).forEach(AbstractVertex::evaluateDataFlow);
        return this.getPreviousElements().stream().flatMap(it -> it.getAllOutgoingDataFlowVariables().stream()).collect(Collectors.toList());
    }

    /**
     * Calculate the vertex characteristics for the sequence element
     * @return Returns a list of vertex characteristics that are applied to the pcm vertex
     */
    protected List<CharacteristicValue> getVertexCharacteristics() {
        PCMVertexCharacteristicsCalculator vertexCharacteristicsCalculator = new PCMVertexCharacteristicsCalculator(this.resourceProvider);
        return vertexCharacteristicsCalculator.getNodeCharacteristics(this.referencedElement, this.context);
    }

    /**
     * Calculate the data characteristics for the vertex with the given vertex characteristics, variable characterizations
     * and old data flow variables
     * @param vertexCharacteristics Vertex characteristics present at the vertex
     * @param variableCharacterisations Variable characterizations present in the model
     * @param oldDataFlowVariables Old data flow variables present at the node
     * @return Returns a list of data characteristics that are applied to the sequence element
     */
    protected List<DataFlowVariable> getDataFlowVariables(List<CharacteristicValue> vertexCharacteristics,
            List<ConfidentialityVariableCharacterisation> variableCharacterisations, List<DataFlowVariable> oldDataFlowVariables) {
        PCMDataCharacteristicsCalculator dataCharacteristicsCalculator = new PCMDataCharacteristicsCalculator(oldDataFlowVariables,
                vertexCharacteristics, this.resourceProvider);
        variableCharacterisations.forEach(dataCharacteristicsCalculator::evaluate);
        return dataCharacteristicsCalculator.getCalculatedCharacteristics();
    }

    /**
     * Checks the parameters to the call signature for characterizations that have no impact on the results at all
     * @param callSignature Call Signature of the call
     * @param variableCharacterisations Variable characterizations that are applied to the sequence element
     */
    protected void checkCallParameter(OperationSignature callSignature, List<ConfidentialityVariableCharacterisation> variableCharacterisations) {
        List<String> parameter = callSignature.getParameters__OperationSignature().stream().map(Parameter::getParameterName).toList();

        List<String> referencedParameter = variableCharacterisations.stream()
                .map(it -> it.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage().getReferenceName()).toList();

        referencedParameter.stream().filter(it -> !parameter.contains(it)).forEach(it -> {
            logger.warn("Unknown reference to variable " + it + " in variable characterisation in vertex " + this.referencedElement);
            logger.warn("Present variables:" + parameter + ", Referenced parameter: " + referencedParameter);
        });
    }

    protected AbstractPCMVertex<?> updateCopy(AbstractPCMVertex<?> copy, Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        if (this.isEvaluated()) {
            copy.setPropagationResult(this.getAllIncomingDataFlowVariables(), this.getAllOutgoingDataFlowVariables(),
                    this.getVertexCharacteristics());
        }
        vertexMapping.put(this, copy);

        List<? extends AbstractPCMVertex<?>> clonedPreviousElements = this.previousElements.stream().map(it -> it.deepCopy(vertexMapping)).toList();

        copy.setPreviousElements(clonedPreviousElements);
        return copy;
    }

    /**
     * Return the referenced element of the vertex
     * @return Returns the referenced element
     */
    public T getReferencedElement() {
        return referencedElement;
    }

    /**
     * Returns the assembly contexts of the vertex
     * @return Returns a {@link Deque} of {@link AssemblyContext}s that the vertex has
     */
    public Deque<AssemblyContext> getContext() {
        return context;
    }

    @Override
    public boolean equals(Object otherVertexObject) {
        if (!(otherVertexObject instanceof AbstractPCMVertex<?> otherVertex)) {
            return false;
        }
        return this.getReferencedElement().getId().equals(otherVertex.getReferencedElement().getId());
    }
}
