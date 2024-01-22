package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

public interface DataFlowConfidentialityAnalysis {

    public boolean initializeAnalysis();

    public List<FlowGraph> findAllSequences();

    public List<FlowGraph> evaluateDataFlows(List<FlowGraph> sequences);

    public List<AbstractActionSequenceElement<?>> queryDataFlow(FlowGraph sequence,
            Predicate<? super AbstractActionSequenceElement<?>> condition);
    
    public void setLoggerLevel(Level level);
}
