package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;

public interface ActionSequenceFinder {

    public List<ActionSequence> findAllSequences();

}
