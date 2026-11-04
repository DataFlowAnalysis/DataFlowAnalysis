package org.dataflowanalysis.analysis.dsl.selectors.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class LogicalGroupOperator extends LogicalOperator {
    private final AbstractSelector selector;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public LogicalGroupOperator(AbstractSelector selector, DSLContext context) {
        super(context);
        this.selector = selector;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return selector.matchesSource(vertex, dslConstraintTrace);
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return selector.matchesDestination(vertex, dslConstraintTrace);
    }

    @Override
    public boolean hasDataSelector() {
        return this.selector.hasDataSelector();
    }

    @Override
    public boolean hasVertexSelector() {
        return this.selector.hasVertexSelector();
    }

    @Override
    public String toString() {
        return DSL_PAREN_OPEN + selector.toString() + DSL_PAREN_CLOSE;
    }
}
