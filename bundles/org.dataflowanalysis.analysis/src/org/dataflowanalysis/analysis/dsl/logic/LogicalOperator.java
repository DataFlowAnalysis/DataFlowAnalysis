package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class LogicalOperator extends AbstractSelector {
    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public LogicalOperator(DSLContext context) {
        super(context);
    }

    public static ParseResult<? extends LogicalOperator> fromString(StringView string, DSLContext context) {
        // Handle precedence and bounding power of logical operators here
        return ParseResult.error("Logical operators parsing not yet implemented!");
    }
}
