package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;

public interface DataFlowConfidentialityAnalysis {
	public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";

    public boolean initializeAnalysis();

    public List<AbstractPartialFlowGraph> findAllPartialFlowGraphs();

    public List<AbstractPartialFlowGraph> evaluateDataFlows(List<AbstractPartialFlowGraph> sequences);

    public List<AbstractVertex<?>> queryDataFlow(AbstractPartialFlowGraph sequence,
            Predicate<? super AbstractVertex<?>> condition);
    
    public void setLoggerLevel(Level level);
}
