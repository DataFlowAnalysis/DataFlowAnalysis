package org.dataflowanalysis.analysis.dsl.selectors.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class AndLogicalOperator extends LogicalOperator {
    private final AbstractSelector leftHandSide;
    private final AbstractSelector rightHandSide;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public AndLogicalOperator(AbstractSelector leftHandSide, AbstractSelector rightHandSide, DSLContext context) {
        super(context);
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return leftHandSide.matchesSource(vertex, dslConstraintTrace)
                && rightHandSide.matchesSource(vertex, dslConstraintTrace);
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return leftHandSide.matchesDestination(vertex, dslConstraintTrace)
                && rightHandSide.matchesDestination(vertex, dslConstraintTrace);
    }

    @Override
    public boolean hasDataSelector() {
        return leftHandSide.hasDataSelector() || rightHandSide.hasDataSelector();
    }

    @Override
    public boolean hasVertexSelector() {
        return leftHandSide.hasVertexSelector() || rightHandSide.hasVertexSelector();
    }

    @Override
    public String toString() {
        return leftHandSide.toString() + " " + DSL_AND + " " + rightHandSide.toString();
    }
}
