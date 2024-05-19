package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a transpose flow graph in the dfd model induced by a sink
 * {@link AbstractTransposeFlowGraph#getSink()}
 */
public class DFDTransposeFlowGraph extends AbstractTransposeFlowGraph {

    /**
     * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public DFDTransposeFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    /**
     * Evaluates the transpose flow graph, beginning at the sink
     * @return Returns a new evaluated transpose flow graph with references to evaluated vertices
     */
    @Override
    public AbstractTransposeFlowGraph evaluate() {
        DFDVertex newSink = ((DFDVertex) sink).clone();
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new DFDTransposeFlowGraph(newSink);
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
        DFDVertex copiedSink = ((DFDVertex) sink).clone();
        copiedSink.unify(new HashSet<>());
        return new DFDTransposeFlowGraph(copiedSink);
    }
}
