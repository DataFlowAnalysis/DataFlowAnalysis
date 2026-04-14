package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

public class NotLogicalOperator extends LogicalOperator {
    private final AbstractSelector selector;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public NotLogicalOperator(AbstractSelector selector, DSLContext context) {
        super(context);
        this.selector = selector;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return !selector.matches(vertex);
    }
}
