package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface TransposeFlowGraphFinder {
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs();

    /**
     * Determine transpose flow graphs starting at the List of start nodes and containing one of the source nodes.
     * <p/>
     * The List of end nodes may be empty to return all transpose flow graphs regardless of what nodes they contain
     * @param sinkNodes List of sink nodes the transpose flow graph should end at
     * @param sourceNodes List of source nodes the transpose flow graph should start at
     * @return Returns a list of all transpose flow graph between the list of source and sink nodes
     */
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs(List<?> sinkNodes, List<?> sourceNodes);
    
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs(List<?> sourceNodes);
}
