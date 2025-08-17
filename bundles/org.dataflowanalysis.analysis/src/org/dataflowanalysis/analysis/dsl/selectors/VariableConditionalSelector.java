package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.List;
import java.util.Optional;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VariableConditionalSelector extends AbstractParseable implements ConditionalSelector {
    private static final String DSL_KEYWORD = "present";

    private final ConstraintVariableReference constraintVariable;
    private final boolean inverted;

    public VariableConditionalSelector(ConstraintVariableReference constraintVariable) {
        this.constraintVariable = constraintVariable;
        this.inverted = false;
    }

    public VariableConditionalSelector(ConstraintVariableReference constraintVariable, boolean inverted) {
        this.constraintVariable = constraintVariable;
        this.inverted = inverted;
    }

    @Override
    public boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context) {
        List<ConstraintVariable> variables = context.getMappings(vertex);
        Optional<ConstraintVariable> variable = variables.stream()
                .filter(it -> it.getName()
                        .equals(this.constraintVariable.name()))
                .findAny();
        if (variable.isEmpty()) {
            return false;
        }
        if (!variable.get()
                .hasValues()) {
            return false;
        }
        return this.inverted == variable.get()
                .getPossibleValues()
                .get()
                .isEmpty();
    }

    public ConstraintVariableReference getConstraintVariable() {
        return constraintVariable;
    }

    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        if (this.inverted) {
            return DSL_KEYWORD + " " + DSL_INVERTED_SYMBOL + this.constraintVariable.toString();
        } else {
            return DSL_KEYWORD + " " + this.constraintVariable.toString();
        }
    }

    /**
     * Parses a {@link VariableConditionalSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code present<Variable>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VariableConditionalSelector} object
     */
    public static ParseResult<VariableConditionalSelector> fromString(StringView string) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse variable conditional selector from empty or invalid string!");
        }
        int position = string.getPosition();
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        if (string.invalid() || string.empty()) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse variable conditional selector from empty/invalid string");
        }
        boolean inverted = string.startsWith(DSL_INVERTED_SYMBOL);
        if (inverted)
            string.advance(DSL_INVERTED_SYMBOL.length());
        ParseResult<ConstraintVariableReference> constraintVariableReference = ConstraintVariableReference.fromString(string);
        if (constraintVariableReference.failed()) {
            string.setPosition(position);
            return ParseResult.error(constraintVariableReference.getError());
        }
        string.advance(1);
        return ParseResult.ok(new VariableConditionalSelector(constraintVariableReference.getResult(), inverted));
    }
}
