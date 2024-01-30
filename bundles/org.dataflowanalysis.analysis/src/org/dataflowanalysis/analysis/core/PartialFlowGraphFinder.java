package org.dataflowanalysis.analysis.core;

import java.util.List;

import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;

public interface PartialFlowGraphFinder {

	/**
	 * Finds all seqences provided by the (otherwise) provided Resources
	 * @return Returns List of action sequences that were found by the finder
	 */
    public List<? extends AbstractPartialFlowGraph> findPartialFlowGraphs();

}
