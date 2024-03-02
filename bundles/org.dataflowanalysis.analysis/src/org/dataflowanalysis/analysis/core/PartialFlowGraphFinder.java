package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface PartialFlowGraphFinder {
    List<? extends AbstractPartialFlowGraph> findPartialFlowGraphs();
}
