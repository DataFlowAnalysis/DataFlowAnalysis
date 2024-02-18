package org.dataflowanalysis.analysis.pcm.core;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.model.confidentiality.ConfidentialityVariableCharacterisation;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.OperationSignature;

public abstract class AbstractPCMVertex<T extends Entity> extends AbstractVertex<T> {
    private final Logger logger = Logger.getLogger(AbstractPCMVertex.class);

    protected final Deque<AssemblyContext> context;
    protected final ResourceProvider resourceProvider;
    protected List<? extends AbstractPCMVertex<?>> previousElements;

    /**
     * Constructs a new abstract pcm vertex with the underlying palladio element and assembly context
     * @param vertex Underlying palladio element of the abstract pcm vertex
     * @param context Assembly context of the abstract pcm vertex
     */
    public AbstractPCMVertex(T referencedElement, Deque<AssemblyContext> context, ResourceProvider resourceProvider) {
        super(referencedElement);
        this.context = context;
        this.resourceProvider = resourceProvider;
        this.previousElements = List.of();
    }

    public abstract AbstractPCMVertex<?> deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism);

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

    /**
     * Constructs a new abstract pcm vertex with the underlying palladio element and assembly context
     * @param vertex Underlying palladio element of the abstract pcm vertex
     * @param context Assembly context of the abstract pcm vertex
     */
    public AbstractPCMVertex(T referencedElement, List<? extends AbstractPCMVertex<?>> previousElements, Deque<AssemblyContext> context,
            ResourceProvider resourceProvider) {
        super(referencedElement);
        this.context = context;
        this.resourceProvider = resourceProvider;
        this.previousElements = previousElements;
    }

    /**
     * Calculate the vertex characteristics for the sequence element with the given node characteristics calculator
     * @param vertexCharacteristicsCalculator Node characteristics calculator that is used to calculate the characteristics
     * for the vertex
     * @return Returns a list of vertex characteristics that are applied to the pcm vertex
     */
    protected List<CharacteristicValue> getVertexCharacteristics() {
        PCMVertexCharacteristicsCalculator vertexCharacteristicsCalculator = new PCMVertexCharacteristicsCalculator(this.resourceProvider);
        return vertexCharacteristicsCalculator.getNodeCharacteristics(this.referencedElement, this.context);
    }

    /**
     * Calculate the data characteristics for the vertex with the given data characteristics calculator, vertex
     * characteristics, variable characterizations and old data flow variables
     * @param dataCharacteristicsCalculatorFactory Data characteristics factory that is used to calculate the data
     * characteristics at the present vertex
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
     * @param callSigniture Call Signature of the call
     * @param variableCharacterisations Variable characterizations that are applied to the sequence element
     */
    protected void checkCallParameter(OperationSignature callSigniture, List<ConfidentialityVariableCharacterisation> variableCharacterisations) {
        List<String> parameter = callSigniture.getParameters__OperationSignature().stream().map(it -> it.getParameterName()).toList();

        List<String> referencedParameter = variableCharacterisations.stream()
                .map(it -> it.getVariableUsage_VariableCharacterisation().getNamedReference__VariableUsage().getReferenceName()).toList();

        referencedParameter.stream().filter(it -> !parameter.contains(it)).forEach(it -> {
            logger.warn("Unknown reference to variable " + it + " in variable characterisation in vertex " + this.referencedElement);
            logger.warn("Present variables:" + parameter + ", Referenced parameter: " + referencedParameter);
        });
    }

    /**
     * Return the saved element of the sequence element
     * @return
     */
    public T getReferencedElement() {
        return referencedElement;
    }

    /**
     * Returns the assembly contexts of the sequence element
     * @return Returns a {@link Deque} of {@link AssemblyContext}s that the sequence element has
     */
    public Deque<AssemblyContext> getContext() {
        return context;
    }
}
