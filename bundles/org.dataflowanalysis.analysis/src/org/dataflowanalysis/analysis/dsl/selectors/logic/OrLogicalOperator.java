package org.dataflowanalysis.analysis.dsl.selectors.logic;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class OrLogicalOperator extends LogicalOperator {
    private final AbstractSelector leftHandSide;
    private final AbstractSelector rightHandSide;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public OrLogicalOperator(AbstractSelector leftHandSide, AbstractSelector rightHandSide, DSLContext context) {
        super(context);
        this.leftHandSide = leftHandSide;
        this.rightHandSide = rightHandSide;
    }

    private List<String> variablePredicate(List<String> lhs, List<String> rhs) {
        List<String> result = new ArrayList<>();
        result.addAll(lhs);
        result.addAll(rhs);
        return result;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        var context = this.context.copy();
        boolean resultLhs = leftHandSide.matchesSource(vertex, dslConstraintTrace);
        var contextLhs = this.context.copy();
        boolean resultRhs = rightHandSide.matchesSource(vertex, dslConstraintTrace);
        var contextRhs = this.context.copy();
        this.context.updateFromLogicalOperation(context, contextLhs, contextRhs, this::variablePredicate);
        return resultLhs || resultRhs;
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        var context = this.context.copy();
        boolean resultLhs = leftHandSide.matchesDestination(vertex, dslConstraintTrace);
        var contextLhs = this.context.copy();
        boolean resultRhs = rightHandSide.matchesDestination(vertex, dslConstraintTrace);
        var contextRhs = this.context.copy();
        this.context.updateFromLogicalOperation(context, contextLhs, contextRhs, this::variablePredicate);
        return resultLhs || resultRhs;
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
        return leftHandSide.toString() + " " + DSL_OR + " " + rightHandSide.toString();
    }
}
