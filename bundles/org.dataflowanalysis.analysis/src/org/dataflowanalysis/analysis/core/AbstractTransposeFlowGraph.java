package org.dataflowanalysis.analysis.core;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This abstract class represents a transpose flow graph induced by a sink {@link AbstractTransposeFlowGraph#getSink()}.
 * Ambiguous flows, like multiple flows to one input pin, are resolved in the transpose flow graph and represent one
 * possible assignment
 */
public abstract class AbstractTransposeFlowGraph {
    protected final AbstractVertex<?> sink;

    /**
     * Create a transpose flow graph induced by the given sink
     * @param sink Sink vertex that induces the transpose flow graph
     */
    public AbstractTransposeFlowGraph(AbstractVertex<?> sink) {
        this.sink = sink;
    }

    /**
     * Evaluate the data flow of the transpose flow graph with the given node and data characteristics calculator
     */
    public abstract AbstractTransposeFlowGraph evaluate();

    /**
     * Returns a copy of the partial flow graph, with all included vertices copied.
     * The references of vertices to the model elements will remain identical
     * @return Returns a copy of the partial flow graph
     */
    public abstract AbstractPartialFlowGraph copy();

    /**
     * Returns the sink that induces the partial flow graph
     * @return Returns the sink that induces the partial flow graph
     */
    public AbstractVertex<?> getSink() {
        return sink;
    }

    /**
     * Returns the saved elements in the sequence
     * @return Returns List of sequence elements, saved in the sequence
     */
    public List<? extends AbstractVertex<?>> getVertices() {
        List<AbstractVertex<?>> vertices = new ArrayList<>();
        Deque<AbstractVertex<?>> currentElements = new ArrayDeque<>();
        currentElements.push(sink);
        while (!currentElements.isEmpty()) {
            AbstractVertex<?> currentElement = currentElements.pop();
            if (vertices.contains(currentElement)) {
                continue;
            }
            vertices.add(currentElement);
            currentElement.getPreviousElements()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(it -> !vertices.contains(it))
                    .forEach(currentElements::push);
        }
        Collections.reverse(vertices);
        return vertices;
    }

    /**
     * This method determines the succeeding vertices of a given vertex. As calculating succeeding vertices is
     * computationally expensive, repeated use of the method is discouraged
     * @param vertex Vertex of which the succeeding vertices should be calculated
     * @return Returns a list of all succeeding vertices
     */
    public List<AbstractVertex<?>> getSucceedingVertices(AbstractVertex<?> vertex) {
        return this.getVertices()
                .stream()
                .filter(it -> it.getPreviousElements()
                        .contains(vertex))
                .collect(Collectors.toList());
    }

    /**
     * Returns a stream over all elements present in the transpose flow graph. The order of elements is completely
     * arbitrary, but deterministic.
     * @return Returns a stream over all elements in the transpose flow graph
     */
    public Stream<? extends AbstractVertex<?>> stream() {
        return this.getVertices()
                .stream();
    }

    @Override
    public String toString() {
        return this.getVertices()
                .stream()
                .map(AbstractVertex::toString)
                .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
