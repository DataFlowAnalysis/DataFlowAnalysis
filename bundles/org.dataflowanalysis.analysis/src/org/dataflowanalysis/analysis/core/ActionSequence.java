package org.dataflowanalysis.analysis.core;

import java.util.List;

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
	 * @param nodeCharacteristicsCalculator Calculator used to calculate the node characteristics of the element
	 * @param dataCharacteristicsCalculatorFactory Calculators used to calculate the data characteristics of elements
	 * @return
	 */
    public abstract ActionSequence evaluateDataFlow(NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
    		DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory);
    
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
