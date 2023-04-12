package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.ActionSequence;

public interface DataFlowConfidentialityAnalysis {

    public boolean initalizeAnalysis();

    public List<ActionSequence> findAllSequences();

    public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences);

    public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
            Predicate<? super AbstractActionSequenceElement<?>> condition);
    
    public void setLoggerLevel(Level level);
}
