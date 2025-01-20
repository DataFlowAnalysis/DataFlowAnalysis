package org.dataflowanalysis.analysis.dfd.simple;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

/**
 * This class represents a transpose flow graph in the dfd model induced by a sink
 * {@link AbstractTransposeFlowGraph#getSink()}
 */
public class DFDSimpleTransposeFlowGraph extends AbstractTransposeFlowGraph {

    /**
     * Creates a new dfd transpose flow graph with the given sink that induces the transpose flow graph
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public DFDSimpleTransposeFlowGraph(AbstractVertex<?> sink) {
        super(sink);
    }

    /**
     * Evaluates the transpose flow graph, beginning at the sink
     * @return Returns a new evaluated transpose flow graph with references to evaluated vertices
     */
    @Override
    public AbstractTransposeFlowGraph evaluate() {
        DFDSimpleVertex newSink = ((DFDSimpleVertex) sink).copy(new IdentityHashMap<>());
        newSink.evaluateDataFlow();
        return new DFDSimpleTransposeFlowGraph(newSink);
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
    	return this.copy(new IdentityHashMap<>());
    }
    
    public AbstractTransposeFlowGraph copy(Map<DFDSimpleVertex, DFDSimpleVertex> mapping) {
        DFDSimpleVertex copiedSink = ((DFDSimpleVertex) sink).copy(mapping);
        return new DFDSimpleTransposeFlowGraph(copiedSink);
    }
}
