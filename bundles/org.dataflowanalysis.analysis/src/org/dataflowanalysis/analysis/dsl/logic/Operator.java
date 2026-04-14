package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public enum Operator {

    NOT((short) 1),
    AND((short) 2),
    XOR((short) 3),
    OR((short) 4);

    public static final short MAX_PRECEDENCE = 4;
    private final short precedence;

    Operator(short precedence) {
        this.precedence = precedence;
    }

    public static ParseResult<Operator> fromString(StringView string) {
        string.skipWhitespace();
        if (string.startsWith(LogicalOperator.DSL_NEGATION)) {
            string.advance(LogicalOperator.DSL_NEGATION.length());
            string.skipWhitespace();
            return ParseResult.ok(Operator.NOT);
        } else if (string.startsWith(LogicalOperator.DSL_AND)) {
            string.advance(LogicalOperator.DSL_AND.length());
            string.skipWhitespace();
            return ParseResult.ok(Operator.AND);
        } else if (string.startsWith(LogicalOperator.DSL_XOR)) {
            string.advance(LogicalOperator.DSL_XOR.length());
            string.skipWhitespace();
            return ParseResult.ok(Operator.XOR);
        } else if (string.startsWith(LogicalOperator.DSL_OR)) {
            string.advance(LogicalOperator.DSL_OR.length());
            string.skipWhitespace();
            return ParseResult.ok(Operator.OR);
        }
        return ParseResult.error("Cannot find valid operator!");
    }

    public short getPrecedence() {
        return precedence;
    }
}
