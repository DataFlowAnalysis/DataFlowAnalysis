package org.dataflowanalysis.analysis.pcm.core;

import java.util.Map;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a partial flow graph that contains pcm vertices and is induced by one sink.
 */
public class PCMPartialFlowGraph extends AbstractPartialFlowGraph {
    /**
     * Creates an empty new action sequence
     */
    public PCMPartialFlowGraph() {
        super(null);
    }

    /**
     * Creates a pcm partial flow graph with the given sink
     * @param sink Sink that induces the partial flow graph
     */
    public PCMPartialFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    @Override
    public AbstractPartialFlowGraph evaluate() {
        this.getSink().evaluateDataFlow();
        return this;
    }

    @Override
    public AbstractPCMVertex<?> getSink() {
        return (AbstractPCMVertex<?>) this.sink;
    }

    public PCMPartialFlowGraph deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> vertexMapping) {
        AbstractPCMVertex<?> pcmSink = (AbstractPCMVertex<?>) this.sink;

        AbstractPCMVertex<?> clonedSink = pcmSink.deepCopy(vertexMapping);

        return new PCMPartialFlowGraph(clonedSink);
    }
}
