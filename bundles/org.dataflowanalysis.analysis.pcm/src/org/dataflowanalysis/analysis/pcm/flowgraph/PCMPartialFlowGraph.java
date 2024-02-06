package org.dataflowanalysis.analysis.pcm.flowgraph;

import java.util.Map;

import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;

/**
 * This class represents a partial flow graph that contains pcm vertices and is induced by one sink.
 *
 */
public class PCMPartialFlowGraph extends AbstractPartialFlowGraph {
	/**
	 * Creates a empty new action sequence
	 */
    public PCMPartialFlowGraph() {
        super(null);
    }
	
	/**
	 * Creates a pcm partial flow graph with the given (temporary) sink
	 * @param elements List of elements contained in the sequence
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
	public AbstractVertex<?> getSink() {
		return this.sink;
	}
	
	public PCMPartialFlowGraph deepCopy(Map<AbstractPCMVertex<?>, AbstractPCMVertex<?>> isomorphism) {
		AbstractPCMVertex<?> pcmSink = (AbstractPCMVertex<?>) this.sink;
		
		AbstractPCMVertex<?> clonedSink = pcmSink.deepCopy(isomorphism);
		
		return new PCMPartialFlowGraph(clonedSink);
	}
}
