package org.dataflowanalysis.analysis.dsl.result;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

import java.util.List;

/**
 * Represents a result of the dsl on a given transpose flow graph
 */
public final class DSLResult {
    private final AbstractTransposeFlowGraph transposeFlowGraph;
    private final List<? extends AbstractVertex<?>> violatingVertices;
    private final DSLConstraintTrace constraintTrace;

    /**
     * Create a new dsl result with the given transpose flow graph, violating vertices and constraint trace
     * @param transposeFlowGraph Given transpose flow graph
     * @param violatingVertices Given list of violating vertices of the transpose flow graph
     * @param constraintTrace Given constraint trace of the transpose flow graph
     */
    public DSLResult(AbstractTransposeFlowGraph transposeFlowGraph, List<? extends AbstractVertex<?>> violatingVertices, DSLConstraintTrace constraintTrace) {
        this.transposeFlowGraph = transposeFlowGraph;
        this.violatingVertices = violatingVertices;
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
     * Returns the violating vertices of the transpose flow graph
     * @return Returns the violating vertices of the transpose flow graph
     */
    public List<? extends AbstractVertex<?>> getViolatingVertices() {
        return violatingVertices;
    }

    /**
     * Returns the constraint trace of the transpose flow graph
     * @return Returns the constraint trace of the transpose flow graph
     */
    public DSLConstraintTrace getConstraintTrace() {
        return constraintTrace;
    }
}
