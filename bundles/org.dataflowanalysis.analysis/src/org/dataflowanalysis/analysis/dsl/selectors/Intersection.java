package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.List;

public class Intersection implements SetOperation {
    private static final String DSL_KEYWORD = "intersection";
    private static final String DSL_PAREN_OPEN = "(";
    private static final String DSL_DELIMITER = ",";
    private static final String DSL_PAREN_CLOSE = ")";
    private static final Logger logger = Logger.getLogger(Intersection.class);

    private final ConstraintVariableReference firstVariable;
    private final ConstraintVariableReference secondVariable;

    public Intersection(ConstraintVariableReference firstVariable, ConstraintVariableReference secondVariable) {
        this.firstVariable = firstVariable;
        this.secondVariable = secondVariable;
    }

    public static Intersection of(ConstraintVariableReference firstVariable, ConstraintVariableReference secondVariable) {
        return new Intersection(firstVariable, secondVariable);
    }

    @Override
    public List<String> match(AbstractVertex<?> vertex, String variableName, DSLContext context) {
        var first = context.getMapping(DSLContextKey.of(variableName, vertex), firstVariable);
        var second = context.getMapping(DSLContextKey.of(variableName, vertex), secondVariable);

        if (!first.hasValues() || !second.hasValues()) {
            return List.of();
        }

        return first.getPossibleValues().get().stream()
                .distinct()
                .filter(it -> second.getPossibleValues().get().contains(it))
                .toList();
    }

    @Override
    public String toString() {
        return DSL_KEYWORD + DSL_PAREN_OPEN + firstVariable.toString() + DSL_DELIMITER + secondVariable.toString() + DSL_PAREN_CLOSE;
    }

    public static ParseResult<Intersection> fromString(StringView string) {
        logger.info("Parsing: " + string.getString());
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length());

        if (!string.startsWith(DSL_PAREN_OPEN)) {
            string.retreat(DSL_KEYWORD.length());
            return string.expect(DSL_PAREN_OPEN);
        }
        string.advance(DSL_PAREN_OPEN.length());

        ParseResult<ConstraintVariableReference> firstConstraintVariableReference = ConstraintVariableReference.fromString(string);
        if (firstConstraintVariableReference.failed()) {
            string.retreat(DSL_KEYWORD.length() + DSL_PAREN_OPEN.length());
            return ParseResult.error(firstConstraintVariableReference.getError());
        }

        if (!string.startsWith(DSL_DELIMITER)) {
            string.retreat(DSL_KEYWORD.length() + DSL_PAREN_OPEN.length() + firstConstraintVariableReference.getResult().toString().length());
            return string.expect(DSL_DELIMITER);
        }
        string.advance(DSL_DELIMITER.length());

        ParseResult<ConstraintVariableReference> secondConstraintVariableReference = ConstraintVariableReference.fromString(string);
        if (firstConstraintVariableReference.failed()) {
            string.retreat(DSL_KEYWORD.length() + DSL_PAREN_OPEN.length() + firstConstraintVariableReference.getResult().toString().length() + DSL_DELIMITER.length());
            return ParseResult.error(firstConstraintVariableReference.getError());
        }

        if (!string.startsWith(DSL_PAREN_CLOSE)) {
            string.retreat(DSL_KEYWORD.length() + DSL_PAREN_OPEN.length() + firstConstraintVariableReference.getResult().toString().length()
                    + DSL_DELIMITER.length() + secondConstraintVariableReference.getResult().toString().length());
            return string.expect(DSL_PAREN_CLOSE);
        }
        string.advance(DSL_PAREN_CLOSE.length());
        return ParseResult.ok(new Intersection(firstConstraintVariableReference.getResult(), secondConstraintVariableReference.getResult()));
    }
}
