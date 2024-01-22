package org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;

public abstract class FlowGraph {
	protected List<AbstractVertex<?>> elements;
	
	/**
	 * Create a new action sequence with the given elements
	 * @param elements List of elements in the sequence
	 */
	public FlowGraph(List<AbstractVertex<?>> elements) {
        this.elements = List.copyOf(elements);
    }
    
	/**
	 * Evaluate the data flow of the action sequence with the given analysis data
	 * @param analysisData Analysis data needed for evaluation
	 * @return
	 */
    public abstract FlowGraph evaluateDataFlow(AnalysisData analysisData);
    
    /**
     * Returns the saved elements in the sequence
     * @return Returns List of sequence elements, saved in the sequence
     */
    public List<AbstractVertex<?>> getElements() {
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
