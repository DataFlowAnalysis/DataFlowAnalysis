package org.dataflowanalysis.analysis.dsl.result;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

import java.util.List;

/**
 * Represents a result of the dsl on a given transpose flow graph
 */
public final class DSLResult {
    private final AbstractTransposeFlowGraph transposeFlowGraph;
    private final List<? extends AbstractVertex<?>> matchingVertices;
    private final DSLConstraintTrace constraintTrace;

    /**
     * Create a new dsl result with the given transpose flow graph, violating vertices and constraint trace
     * @param transposeFlowGraph Given transpose flow graph
     * @param matchingVertices Given list of matched vertices of the transpose flow graph
     * @param constraintTrace Given constraint trace of the transpose flow graph
     */
    public DSLResult(AbstractTransposeFlowGraph transposeFlowGraph, List<? extends AbstractVertex<?>> matchingVertices, DSLConstraintTrace constraintTrace) {
        this.transposeFlowGraph = transposeFlowGraph;
        this.matchingVertices = matchingVertices;
        this.constraintTrace = constraintTrace;
    }

    /**
     * Returns the transpose flow graph of the dsl result
     * @return Returns the transpose flow graph of the dsl result
     */
    public AbstractTransposeFlowGraph getTransposeFlowGraph() {
        return transposeFlowGraph;
    }

    /**
     * Returns the matched vertices of the transpose flow graph
     * @return Returns the matched vertices of the transpose flow graph
     */
    public List<? extends AbstractVertex<?>> getMatchedVertices() {
        return matchingVertices;
    }

    /**
     * Returns the constraint trace of the transpose flow graph
     * @return Returns the constraint trace of the transpose flow graph
     */
    public DSLConstraintTrace getConstraintTrace() {
        return constraintTrace;
    }
}
