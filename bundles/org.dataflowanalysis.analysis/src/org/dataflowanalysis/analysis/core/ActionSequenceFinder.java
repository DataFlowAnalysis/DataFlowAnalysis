package org.dataflowanalysis.analysis.core;

import java.util.List;

public interface ActionSequenceFinder {

	/**
	 * Finds all seqences provided by the (otherwise) provided Resources
	 * @return Returns List of action sequences that were found by the finder
	 */
    public List<? extends ActionSequence> findAllSequences();

}
