package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface TransposeFlowGraphFinder {
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs();

    /**
     * Determine transpose flow graphs starting at the List of start nodes and containing one of the source nodes.
     * <p/>
     * The List of end nodes may be empty to return all transpose flow graphs regardless of what nodes they contain
     * @param sinkNodes
     * @param sourceNodes
     * @return
     */
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs(List<?> sinkNodes, List<?> sourceNodes);
}
