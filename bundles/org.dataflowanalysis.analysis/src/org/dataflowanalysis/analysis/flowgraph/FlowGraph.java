package org.dataflowanalysis.analysis.flowgraph;

import java.util.List;

/**
 * This class represents a abstract flow graph that contains all flows contained in a model.
 * The method {@link FlowGraph#findPartialFlowGraphs()} will be called to determine the partial flow graphs for the specific implementation of the flow graph.
 *
 * TODO: Finding the partial flow graphs needs to happen at the _END_ of the constructors, as {@link FlowGraph#findPartialFlowGraphs()} needs to use attributes from the subclass
 */
public interface FlowGraph {
	List<AbstractPartialFlowGraph> findPartialFlowGraphs();
	
	FlowGraph evaluate();
	
	List<AbstractPartialFlowGraph> getPartialFlowGraphs();
}
