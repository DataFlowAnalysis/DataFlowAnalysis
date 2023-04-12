package org.palladiosimulator.dataflow.confidentiality.analysis.sequence;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.ActionSequence;

public interface ActionSequenceFinder {

	/**
	 * Finds all seqences provided by the (otherwise) provided Resources
	 * @return Returns List of action sequences that were found by the finder
	 */
    public List<? extends ActionSequence> findAllSequences();

}
