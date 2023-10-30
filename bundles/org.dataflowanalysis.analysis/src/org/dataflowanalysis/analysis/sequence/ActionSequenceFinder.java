package org.dataflowanalysis.analysis.sequence;

import java.util.List;

import org.dataflowanalysis.analysis.entity.sequence.ActionSequence;

public interface ActionSequenceFinder {

	/**
	 * Finds all seqences provided by the (otherwise) provided Resources
	 * @return Returns List of action sequences that were found by the finder
	 */
    public List<? extends ActionSequence> findAllSequences();

}
