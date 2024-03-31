package org.dataflowanalysis.analysis.pcm.core;

import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractTransposedFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a transposed flow graph that contains pcm vertices and is induced by one sink.
 */
public class PCMTransposedFlowGraph extends AbstractTransposedFlowGraph {
    /**
     * Creates an empty new pcm transposed flow graph
     */
    public PCMTransposedFlowGraph() {
        super(null);
    }

    /**
     * Creates a pcm transposed flow graph with the given sink
     * @param sink Sink that induces the transposed flow graph
     */
    public PCMTransposedFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    @Override
    public AbstractTransposedFlowGraph evaluate() {
        this.getSink().evaluateDataFlow();
        return this;
    }

    @Override
    public AbstractPCMVertex<?> getSink() {
        return (AbstractPCMVertex<?>) this.sink;
    }

    public PCMTransposedFlowGraph deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        AbstractPCMVertex<?> pcmSink = (AbstractPCMVertex<?>) this.sink;

        AbstractPCMVertex<?> clonedSink = pcmSink.deepCopy(vertexMapping);

        return new PCMTransposedFlowGraph(clonedSink);
    }
}
