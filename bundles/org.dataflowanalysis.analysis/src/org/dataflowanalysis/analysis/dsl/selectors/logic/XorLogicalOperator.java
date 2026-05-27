package org.dataflowanalysis.analysis.dsl.selectors.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class XorLogicalOperator extends LogicalOperator {
    private final AbstractSelector lhs;
    private final AbstractSelector rhs;

    public XorLogicalOperator(AbstractSelector lhs, AbstractSelector rhs, DSLContext context) {
        super(context);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return lhs.matchesSource(vertex, dslConstraintTrace) ^ rhs.matchesSource(vertex, dslConstraintTrace);
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return lhs.matchesDestination(vertex, dslConstraintTrace) ^ rhs.matchesDestination(vertex, dslConstraintTrace);
    }

    @Override
    public boolean hasDataSelector() {
        return lhs.hasDataSelector() || rhs.hasDataSelector();
    }

    @Override
    public boolean hasVertexSelector() {
        return lhs.hasVertexSelector() || rhs.hasVertexSelector();
    }

    @Override
    public String toString() {
        return lhs.toString() + " " + DSL_XOR + " " + rhs.toString();
    }
}
