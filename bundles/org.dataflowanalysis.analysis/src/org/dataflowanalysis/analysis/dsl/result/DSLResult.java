package org.dataflowanalysis.analysis.dsl.result;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

import java.util.List;

public final class DSLResult {
    private final AbstractTransposeFlowGraph transposeFlowGraph;
    private final List<? extends AbstractVertex<?>> violatingVertices;
    private final DSLConstraintTrace constraintTrace;

    public DSLResult(AbstractTransposeFlowGraph transposeFlowGraph, List<? extends AbstractVertex<?>> violatingVertices, DSLConstraintTrace constraintTrace) {
        this.transposeFlowGraph = transposeFlowGraph;
        this.violatingVertices = violatingVertices;
        this.constraintTrace = constraintTrace;
    }

    public AbstractTransposeFlowGraph getTransposeFlowGraph() {
        return transposeFlowGraph;
    }

    public List<? extends AbstractVertex<?>> getViolatingVertices() {
        return violatingVertices;
    }

    public DSLConstraintTrace getConstraintTrace() {
        return constraintTrace;
    }
}
