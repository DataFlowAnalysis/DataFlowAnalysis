package org.dataflowanalysis.analysis.dsl.logic;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class LogicalOperator extends AbstractSelector {
    protected static final String DSL_NEGATION = "!";
    protected static final String DSL_AND = "&";
    protected static final String DSL_XOR = "^";
    protected static final String DSL_OR = "|";

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public LogicalOperator(DSLContext context) {
        super(context);
    }

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context,
            boolean data) {
        var leftHandSide = LogicalOperator.fromString(string, context, data, Operator.MAX_PRECEDENCE);
        if (leftHandSide.failed()) {
            return ParseResult.error("Cannot parse logical expression from string");
        }
        return ParseResult.ok(leftHandSide.getResult());
    }

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context,
            boolean data, short precedence) {
        if (precedence == 1) {
            return LogicalOperator.parseUnaryOperation(string, context, data, precedence);
        } else if (precedence == 0) {
            return LogicalOperator.parseBasicExpression(string, context, data);
        }

        var leftHandSideParseResult = LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
        if (leftHandSideParseResult.failed()) {
            return ParseResult.error("Cannot parse logical expression from string");
        }
        var leftHandSide = leftHandSideParseResult.getResult();
        while (!string.invalid() && !string.empty()) {
            int position = string.getPosition();
            var operator = Operator.fromString(string);
            if (operator.failed() || operator.getResult()
                    .getPrecedence() != precedence) {
                string.setPosition(position);
                return ParseResult.ok(leftHandSide);
            }
            switch (operator.getResult()) {
                case NOT -> {
                }
                case OR -> {
                    var rightHandSide = LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
                    if (rightHandSide.failed()) {
                        return ParseResult.error("Cannot parse logical expression from string");
                    }
                    leftHandSide = new OrLogicalOperator(leftHandSide, rightHandSide.getResult(), context);
                    continue;
                }
                case AND -> {
                    var rightHandSide = LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
                    if (rightHandSide.failed()) {
                        return ParseResult.error("Cannot parse logical expression from string");
                    }
                    leftHandSide = new AndLogicalOperator(leftHandSide, rightHandSide.getResult(), context);
                    continue;
                }
                case XOR -> {
                    var rightHandSide = LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
                    if (rightHandSide.failed()) {
                        return ParseResult.error("Cannot parse logical expression from string");
                    }
                    leftHandSide = new XorLogicalOperator(leftHandSide, rightHandSide.getResult(), context);
                    continue;
                }
            }
            string.setPosition(position);
            return ParseResult.ok(leftHandSide);
        }
        return ParseResult.ok(leftHandSide);
    }

    public static ParseResult<? extends AbstractSelector> parseUnaryOperation(StringView string, DSLContext context,
            boolean data, short precedence) {
        int position = string.getPosition();
        var operator = Operator.fromString(string);
        if (operator.failed()) {
            string.setPosition(position);
            return LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
        }
        switch (operator.getResult()) {
            case NOT -> {
                var selector = LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
                if (selector.failed()) {
                    string.setPosition(position);
                    return ParseResult.error("Cannot parse logical expression from string");
                }
                return ParseResult.ok(new NotLogicalOperator(selector.getResult(), context));
            }
            case OR, AND, XOR -> {
                string.setPosition(position);
                return LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
            }
        }
        string.setPosition(position);
        return LogicalOperator.fromString(string, context, data, (short) (precedence - 1));
    }

    public static ParseResult<? extends AbstractSelector> parseBasicExpression(StringView string, DSLContext context,
            boolean data) {
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot get expression from basic string!");
        }
        if (string.startsWith(DSL_PAREN_OPEN)) {
            string.advance(DSL_PAREN_OPEN.length());
            var expression = LogicalOperator.fromString(string, context, data);
            if (string.empty() || string.invalid()) {
                return ParseResult.error("No closing bracket after expression");
            }
            if (!string.startsWith(DSL_PAREN_CLOSE)) {
                return string.expect(DSL_PAREN_CLOSE);
            }
            string.advance(DSL_PAREN_CLOSE.length());
            return expression;
        }
        return AbstractSelector.fromString(string, context, data);
    }
}
