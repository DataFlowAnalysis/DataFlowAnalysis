package org.dataflowanalysis.analysis.core;

import java.util.List;
import java.util.stream.Stream;

/**
 * This abstract class represents a partial flow graph induced by a sink {@link AbstractPartialFlowGraph#getSink()}.
 * Ambiguous flows, like multiple flows to one input pin, are resolved in the partial flow graph and represent one possible assignment
 * TODO: Actually implement correct representation
 *
 */
public abstract class AbstractPartialFlowGraph {
	// TODO: Implement correct attributes for partial flow graph. We should try making partial flow graphs immutable (e.g. most attributes should be final)
	protected List<AbstractVertex<?>> vertices;
	
	/**
	 * Create a new action sequence with the given elements
	 * @param elements List of elements in the sequence
	 * TODO: Adapt constructor to new implementation
	 */
	public AbstractPartialFlowGraph(List<AbstractVertex<?>> vertices) {
        this.vertices = List.copyOf(vertices);
    }
    
	/**
	 * Evaluate the data flow of the partial flow graph with the given node and data characteristics calculator
	 * @param nodeCharacteristicsCalculator Calculator used to calculate the node characteristics of the element
	 * @param dataCharacteristicsCalculatorFactory Calculators used to calculate the data characteristics of elements
	 * @return Returns the evaluated partial flow graph in which vertex and data characteristics were calculated
	 */
    public abstract AbstractPartialFlowGraph evaluate(VertexCharacteristicsCalculator nodeCharacteristicsCalculator, 
    		DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory);
    
    /**
     * Returns the saved elements in the sequence
     * @return Returns List of sequence elements, saved in the sequence
     * TODO: Remove this method from the new implementation, as it might be misleading?
     */
    public List<AbstractVertex<?>> getVertices() {
		return vertices;
	}
    
    /**
     * Returns a stream over all elements present in the partial flow graph.
     * The order of elements is completely arbitrary, but deterministic.
     * @return Returns a stream over all elements in the partial flow graph
     * TODO: Implement the stream behavior for the new representation
     */
    public Stream<AbstractVertex<?>> stream() {
    	return this.getVertices().parallelStream();
    }
    
    @Override
    public String toString() {
        return this.getVertices()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
