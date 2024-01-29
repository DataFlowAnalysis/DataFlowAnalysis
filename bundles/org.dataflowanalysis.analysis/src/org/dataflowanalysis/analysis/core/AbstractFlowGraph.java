package org.dataflowanalysis.analysis.core;

import java.util.List;

/**
 * This class represents a abstract flow graph that contains all flows contained in a model.
 * The method {@link AbstractFlowGraph#findPartialFlowGraphs()} will be called to determine the partial flow graphs for the specific implementation of the flow graph.
 *
 */
public abstract class AbstractFlowGraph {
	private final List<AbstractPartialFlowGraph> partialFlowGraphs;
	
	public AbstractFlowGraph() {
		this.partialFlowGraphs = findPartialFlowGraphs();
	}
	
	protected abstract List<AbstractPartialFlowGraph> findPartialFlowGraphs();
	
	public List<AbstractPartialFlowGraph> getPartialFlowGraphs() {
		return partialFlowGraphs;
	}
}
