package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.PCMActionSequence;

public interface ActionSequenceFinder {

    public List<PCMActionSequence> findAllSequences();

}
