package org.dataflowanalysis.analysis.pcm.core;

import java.util.IdentityHashMap;
import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a transpose flow graph that contains pcm vertices and is induced by one sink.
 */
public class PCMTransposeFlowGraph extends AbstractTransposeFlowGraph {
    /**
     * Creates an empty new pcm transpose flow graph
     */
    public PCMTransposeFlowGraph() {
        super(null);
    }

    /**
     * Creates a pcm transpose flow graph with the given sink
     * @param sink Sink that induces the transpose flow graph
     */
    public PCMTransposeFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    @Override
    public AbstractTransposeFlowGraph evaluate() {
        this.getSink()
                .evaluateDataFlow();
        return this;
    }

    @Override
    public AbstractPCMVertex<?> getSink() {
        return (AbstractPCMVertex<?>) this.sink;
    }

    @Override
    public PCMTransposeFlowGraph copy() {
        return this.copy(new IdentityHashMap<>());
    }

    public PCMTransposeFlowGraph copy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        AbstractPCMVertex<?> pcmSink = (AbstractPCMVertex<?>) this.sink;
        AbstractPCMVertex<?> clonedSink = pcmSink.copy(vertexMapping);

        return new PCMTransposeFlowGraph(clonedSink);
    }
}
