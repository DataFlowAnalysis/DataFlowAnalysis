package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class AndLogicalOperator extends LogicalOperator {
    private static final String DSL_OPERATOR = "&&";

    private final AbstractSelector lhs;
    private final AbstractSelector rhs;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public AndLogicalOperator(AbstractSelector lhs, AbstractSelector rhs, DSLContext context) {
        super(context);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return lhs.matches(vertex) && rhs.matches(vertex);
    }
}
