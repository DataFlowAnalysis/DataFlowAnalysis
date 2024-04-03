package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface TransposeFlowGraphFinder {
    List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs();
}
