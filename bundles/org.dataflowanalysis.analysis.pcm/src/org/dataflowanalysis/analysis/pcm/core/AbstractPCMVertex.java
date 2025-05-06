package org.dataflowanalysis.analysis.pcm.core;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
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

    public abstract AbstractPCMVertex<?> copy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping);

    /**
     * Sets the propagation result of the Vertex to the given result. This method should only be called once on elements
     * that are not evaluated.
     * @param incomingDataCharacteristics Incoming data characteristics that flow into the vertex
     * @param outgoingDataCharacteristics Outgoing data characteristics that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    @Override
    protected void setPropagationResult(List<DataCharacteristic> incomingDataCharacteristics, List<DataCharacteristic> outgoingDataCharacteristics,
            List<CharacteristicValue> vertexCharacteristics) {
        super.setPropagationResult(incomingDataCharacteristics, outgoingDataCharacteristics, vertexCharacteristics);
    }

    @Override
    public List<? extends AbstractPCMVertex<?>> getPreviousElements() {
        return this.previousElements;
    }

    public void setPreviousElements(List<? extends AbstractPCMVertex<?>> previousElements) {
        this.previousElements = previousElements;
    }

    protected List<DataCharacteristic> getIncomingDataCharacteristics() {
        if (super.isSource())
            return List.of();

        this.getPreviousElements()
                .stream()
                .filter(it -> !it.isEvaluated())
                .forEach(AbstractVertex::evaluateDataFlow);
        return this.getPreviousElements()
                .stream()
                .flatMap(it -> it.getAllOutgoingDataCharacteristics()
                        .stream())
                .collect(Collectors.toList());
    }

    /**
     * Calculate the vertex characteristics for the sequence element
     * @return Returns a list of vertex characteristics that are applied to the pcm vertex
     */
    protected List<CharacteristicValue> getVertexCharacteristics() {
        PCMVertexCharacteristicsCalculator vertexCharacteristicsCalculator = new PCMVertexCharacteristicsCalculator(this.resourceProvider);
        return vertexCharacteristicsCalculator.getVertexCharacteristics(this.referencedElement, this.context);
    }

    /**
     * Calculate the data characteristics for the vertex with the given vertex characteristics, variable characterizations
     * and old data characteristics
     * @param vertexCharacteristics Vertex characteristics present at the vertex
     * @param variableCharacterisations Variable characterizations present in the model
     * @param oldDataCharacteristics Old data characteristics present at the node
     * @return Returns a list of data characteristics that are applied to the sequence element
     */
    protected List<DataCharacteristic> getDataCharacteristics(List<CharacteristicValue> vertexCharacteristics,
            List<ConfidentialityVariableCharacterisation> variableCharacterisations, List<DataCharacteristic> oldDataCharacteristics) {
        PCMDataCharacteristicsCalculator dataCharacteristicsCalculator = new PCMDataCharacteristicsCalculator(oldDataCharacteristics,
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
        List<String> parameter = callSignature.getParameters__OperationSignature()
                .stream()
                .map(Parameter::getParameterName)
                .toList();

        List<String> referencedParameter = variableCharacterisations.stream()
                .map(it -> it.getVariableUsage_VariableCharacterisation()
                        .getNamedReference__VariableUsage()
                        .getReferenceName())
                .toList();

        referencedParameter.stream()
                .filter(it -> !parameter.contains(it))
                .forEach(it -> {
                    logger.warn("Unknown reference to variable " + it + " in variable characterisation in vertex " + this.referencedElement);
                    logger.warn("Present variables:" + parameter + ", Referenced parameter: " + referencedParameter);
                });
    }

    protected AbstractPCMVertex<?> updateCopy(AbstractPCMVertex<?> copy, Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        if (this.isEvaluated()) {
            copy.setPropagationResult(this.getAllIncomingDataCharacteristics(), this.getAllOutgoingDataCharacteristics(),
                    this.getVertexCharacteristics());
        }
        vertexMapping.put(this, copy);

        List<? extends AbstractPCMVertex<?>> clonedPreviousElements = this.previousElements.stream()
                .map(it -> it.copy(vertexMapping))
                .toList();

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
     * Return the stored resource provider
     * @return Return the stored resource provider
     */
    public ResourceProvider getResourceProvider() {
        return resourceProvider;
    }

    /**
     * Returns the assembly contexts of the vertex
     * @return Returns a {@link Deque} of {@link AssemblyContext}s that the vertex has
     */
    public Deque<AssemblyContext> getContext() {
        return context;
    }


    /**
     * Determines whether a vertex is equivalent to another vertex in the context of the PCM model
     * <p/>
     * A vertex is equivalent in the PCM context, when the id of the referenced PCM elements is equal and the
     * <i>direction<i/> (e.g. calling, returning) is equal
     * @param otherVertexObject Other vertex object that is used in the comparison
     * @return Returns true, when vertices are equivalent in the context of PCM elements
     */
    public boolean isEquivalentInContext(Object otherVertexObject) {
        if (!(otherVertexObject instanceof AbstractPCMVertex<?> otherVertex)) {
            return false;
        }
        return this.getReferencedElement()
                .getId()
                .equals(otherVertex.getReferencedElement()
                        .getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getReferencedElement()
                .getId());
    }
}
