package org.dataflowanalysis.analysis.entity.sequence;

import java.util.List;

import org.dataflowanalysis.analysis.builder.AnalysisData;

public abstract class ActionSequence {
	protected List<AbstractActionSequenceElement<?>> elements;
	
	/**
	 * Create a new action sequence with the given elements
	 * @param elements List of elements in the sequence
	 */
	public ActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        this.elements = List.copyOf(elements);
    }
    
	/**
	 * Evaluate the data flow of the action sequence with the given analysis data
	 * @param analysisData Analysis data needed for evaluation
	 * @return
	 */
    public abstract ActionSequence evaluateDataFlow(AnalysisData analysisData);
    
    /**
     * Returns the saved elements in the sequence
     * @return Returns List of sequence elements, saved in the sequence
     */
    public List<AbstractActionSequenceElement<?>> getElements() {
		return elements;
	}
    
    @Override
    public String toString() {
        return this.getElements()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
