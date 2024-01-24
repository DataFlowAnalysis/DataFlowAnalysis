package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.PartialFlowGraph;

public interface DataFlowConfidentialityAnalysis {
	public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";

    public boolean initializeAnalysis();

    public List<PartialFlowGraph> findAllPartialFlowGraphs();

    public List<PartialFlowGraph> evaluateDataFlows(List<PartialFlowGraph> sequences);

    public List<AbstractVertex<?>> queryDataFlow(PartialFlowGraph sequence,
            Predicate<? super AbstractVertex<?>> condition);
    
    public void setLoggerLevel(Level level);
}
