package org.dataflowanalysis.analysis.flowgraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This abstract class represents a partial flow graph induced by a sink {@link AbstractPartialFlowGraph#getSink()}.
 * Ambiguous flows, like multiple flows to one input pin, are resolved in the partial flow graph and represent one possible assignment
 *
 */
public abstract class AbstractPartialFlowGraph {
	protected final AbstractVertex<?> sink;
	
	/**
	 * Create a new action sequence with the given elements
	 * @param elements List of elements in the sequence
	 */
	public AbstractPartialFlowGraph(AbstractVertex<?> sink) {
        this.sink = sink;
    }
    
	/**
	 * Evaluate the data flow of the partial flow graph with the given node and data characteristics calculator
	 * @return Returns the evaluated partial flow graph in which vertex and data characteristics were calculated
	 */
    public abstract AbstractPartialFlowGraph evaluate();
    
    public AbstractVertex<?> getSink() {
		return sink;
	}
    
    /**
     * Returns the saved elements in the sequence
     * @return Returns List of sequence elements, saved in the sequence
     * TODO: Return a set of vertices, no order
     */
    public List<? extends AbstractVertex<?>> getVertices() {
    	List<AbstractVertex<?>> vertices = new ArrayList<>();
    	Deque<AbstractVertex<?>> currentElements = new ArrayDeque<>();
    	currentElements.push(sink);
    	while (!currentElements.isEmpty()) {
    		AbstractVertex<?> currentElement = currentElements.pop();
    		vertices.add(currentElement);
    		currentElement.getPreviousElements().stream()
    			.filter(Objects::nonNull)
    			.forEach(currentElements::push);
    	}
    	Collections.reverse(vertices);
		return vertices;
	}
    
    /**
     * Returns a stream over all elements present in the partial flow graph.
     * The order of elements is completely arbitrary, but deterministic.
     * @return Returns a stream over all elements in the partial flow graph
     */
    public Stream<? extends AbstractVertex<?>> stream() {
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
