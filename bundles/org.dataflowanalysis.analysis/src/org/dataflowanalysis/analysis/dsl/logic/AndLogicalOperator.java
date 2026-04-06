package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class AndLogicalOperator extends LogicalOperator {
    private static final String DSL_OPERATOR = "&&";

    private final AbstractSelector lhs;
    private final AbstractSelector rhs;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public AndLogicalOperator(DSLContext context, AbstractSelector lhs, AbstractSelector rhs) {
        super(context);
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        throw new RuntimeException("Not implemented exception");
    }

    public static ParseResult<AndLogicalOperator> fromString(StringView string, DSLContext context) {
        int position = string.getPosition();
        var lhs = AbstractSelector.fromString(string, context);
        if (lhs.failed()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse LHS");
        }
        string.skipWhitespace();
        if (string.startsWith(DSL_OPERATOR)) {
            return string.expect(DSL_OPERATOR);
        }
        string.skipWhitespace();
        var rhs = AbstractSelector.fromString(string, context);
        if (rhs.failed()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse RHS");
        }
        return ParseResult.ok(new AndLogicalOperator(context, lhs.getResult(), rhs.getResult()));
    }
}
