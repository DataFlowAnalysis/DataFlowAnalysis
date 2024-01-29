package org.dataflowanalysis.analysis.core;

import java.util.List;

/**
 * This class represents a abstract flow graph that contains all flows contained in a model.
 * The method {@link FlowGraph#findPartialFlowGraphs()} will be called to determine the partial flow graphs for the specific implementation of the flow graph.
 *
 */
public interface FlowGraph {
	List<AbstractPartialFlowGraph> findPartialFlowGraphs();
	
	FlowGraph evaluate();
	
	List<AbstractPartialFlowGraph> getPartialFlowGraphs();
}
