package org.dataflowanalysis.analysis.core;

import java.util.List;

public abstract class PartialFlowGraph {
	protected List<AbstractVertex<?>> vertices;
	
	/**
	 * Create a new partial flow graph with the given vertices
	 * @param elements List of vertices in the partial flow graph
	 */
	public PartialFlowGraph(List<AbstractVertex<?>> vertices) {
        this.vertices = List.copyOf(vertices);
    }
    
	/**
	 * Evaluate the data flow of the partial flow graph with the given analysis data
	 * @param nodeCharacteristicsCalculator Calculator used to calculate the node characteristics of the vertices
	 * @param dataCharacteristicsCalculatorFactory Calculators used to calculate the data characteristics of vertices
	 * @return
	 */
    public abstract PartialFlowGraph evaluateDataFlow(NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
    		DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory);
    
    /**
     * Returns the saved vertices in the partial flow graph
     * @return Returns List of vertices, saved in the partial flow graph
     */
    public List<AbstractVertex<?>> getVertices() {
		return vertices;
	}
    
    @Override
    public String toString() {
        return this.getVertices()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
