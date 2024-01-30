package org.dataflowanalysis.analysis.core;

import java.util.List;

import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;

public interface PartialFlowGraphFinder {

	/**
	 * Finds all partial flow graphs provided by the (otherwise) provided Resources
	 * @return Returns List of partial flow graphs that were found by the finder
	 */
    public List<? extends AbstractPartialFlowGraph> findPartialFlowGraphs();

}
