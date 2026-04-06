package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public class NotLogicalOperator extends LogicalOperator {
    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public NotLogicalOperator(DSLContext context) {
        super(context);
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return false;
    }
}
