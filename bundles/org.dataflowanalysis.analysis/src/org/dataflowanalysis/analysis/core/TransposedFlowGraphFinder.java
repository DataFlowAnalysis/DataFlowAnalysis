package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface TransposedFlowGraphFinder {
    List<? extends AbstractTransposedFlowGraph> findTransposedFlowGraphs();
}
