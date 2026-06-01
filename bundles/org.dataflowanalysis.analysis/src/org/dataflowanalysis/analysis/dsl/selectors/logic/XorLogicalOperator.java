package org.dataflowanalysis.analysis.dsl.selectors.logic;

import java.util.ArrayList;
import java.util.List;
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

    private List<String> variablePredicate(List<String> lhs, List<String> rhs) {
        List<String> result = new ArrayList<>();
        result.addAll(lhs);
        result.addAll(rhs);
        List<String> intersection = lhs.stream()
                .filter(rhs::contains)
                .toList();
        result.removeAll(intersection);
        return result;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        var context = this.context.copy();
        boolean resultLhs = lhs.matchesSource(vertex, dslConstraintTrace);
        var contextLhs = this.context.copy();
        boolean resultRhs = rhs.matchesSource(vertex, dslConstraintTrace);
        var contextRhs = this.context.copy();
        this.context.updateFromLogicalOperation(context, contextLhs, contextRhs, this::variablePredicate);
        return resultLhs ^ resultRhs;
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        boolean resultLhs = lhs.matchesDestination(vertex, dslConstraintTrace);
        var contextLhs = this.context.copy();
        boolean resultRhs = rhs.matchesDestination(vertex, dslConstraintTrace);
        var contextRhs = this.context.copy();
        this.context.updateFromLogicalOperation(context, contextLhs, contextRhs, this::variablePredicate);
        return resultLhs ^ resultRhs;
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
