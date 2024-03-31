package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;
import org.dataflowanalysis.analysis.core.AbstractTransposedFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a transposed flow graph in the dfd model induced by a sink
 * {@link AbstractTransposedFlowGraph#getSink()}
 */
public class DFDTransposedFlowGraph extends AbstractTransposedFlowGraph {

    /**
     * Creates a new dfd transposed flow graph with the given sink that induces the transposed flow graph
     * @param sink Sink vertex that induces the transposed flow graph
     */
    public DFDTransposedFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    /**
     * Evaluates the transposed flow graph, beginning at the sink
     * @return Returns a new evaluated transposed flow graph with references to evaluated vertices
     */
    @Override
    public AbstractTransposedFlowGraph evaluate() {
        DFDVertex newSink = ((DFDVertex) sink).clone();
        newSink.unify(new HashSet<>());
        newSink.evaluateDataFlow();
        return new DFDTransposedFlowGraph(newSink);
    }

}
