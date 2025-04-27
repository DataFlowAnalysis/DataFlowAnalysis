package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

import org.dataflowanalysis.dfd.datadictionary.Pin;

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
        DFDVertex newSink = cloneSink((DFDVertex)super.sink, new HashMap<>());
        newSink.evaluateDataFlow();
        return new DFDTransposeFlowGraph(newSink);
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
    	return this.copy(new IdentityHashMap<>());
    }
    
    public AbstractTransposeFlowGraph copy(Map<DFDVertex, DFDVertex> mapping) {
        DFDVertex copiedSink = cloneSink((DFDVertex)super.sink, mapping);
        return new DFDTransposeFlowGraph(copiedSink);
    }
    
    private DFDVertex cloneSink(DFDVertex vertex, Map<DFDVertex, DFDVertex> mapping) {
    	if (mapping.containsKey(vertex)) {
    		var test = mapping.get(vertex);
    		System.out.println(test);
    		return test;
    	} 
    	
    	var newMap = new IdentityHashMap<Pin, DFDVertex>();
    	vertex.getPinDFDVertexMap().forEach((pin, v) -> {
    		var newVertex = cloneSink(v, mapping);
    		newMap.put(pin, newVertex);
    		mapping.put(v, newVertex);
    	});
    	
    	return new DFDVertex(vertex.getReferencedElement(), newMap, new IdentityHashMap<>(vertex.getPinFlowMap()));
    }
}
