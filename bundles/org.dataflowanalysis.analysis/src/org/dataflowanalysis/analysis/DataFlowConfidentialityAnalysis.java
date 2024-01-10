package org.dataflowanalysis.analysis;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;

public interface DataFlowConfidentialityAnalysis {
	public static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";

    public boolean initializeAnalysis();

    public List<ActionSequence> findAllSequences();

    public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences);

    public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
            Predicate<? super AbstractActionSequenceElement<?>> condition);
    
    public void setLoggerLevel(Level level);
}
