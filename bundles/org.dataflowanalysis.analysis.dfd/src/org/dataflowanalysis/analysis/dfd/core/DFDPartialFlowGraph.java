package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a partial flow graph in the dfd model induced by a sink
 * {@link AbstractPartialFlowGraph#getSink()}
 */
public class DFDPartialFlowGraph extends AbstractPartialFlowGraph {

    /**
     * Creates a new dfd partial flow graph with the given sink that induces the partial flow graph
     * @param sink Sink vertex that induces the partial flow graph
     */
    public DFDPartialFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    /**
     * Evaluates the partial flow graph, beginning at the sink
     * @return Returns a new evaluated partial flow graph with references to evaluated vertices
     */
    @Override
    public AbstractPartialFlowGraph evaluate() {
        DFDVertex newSink = ((DFDVertex) sink).clone();
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new DFDPartialFlowGraph(newSink);
    }

    @Override
    public AbstractPartialFlowGraph copy() {
        DFDVertex copiedSink = ((DFDVertex) sink).clone();
        copiedSink.unify(new HashSet<>());
        return new DFDPartialFlowGraph(copiedSink);
    }


}
