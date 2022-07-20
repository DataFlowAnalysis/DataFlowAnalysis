package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;

public interface DataFlowConfidentialityAnalysis {

    public boolean initalizeAnalysis();

    public boolean loadModels();

    // only for testing purposes
    public List<ActionSequence> findAllSequences();
    
    // TODO: Add more (real) signatures

}
