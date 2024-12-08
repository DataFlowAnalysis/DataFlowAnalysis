package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

public class Intersection implements SetOperation {
    private static final String DSL_KEYWORD = "intersection";
    private static final String DSL_PAREN_OPEN = "(";
    private static final String DSL_DELIMITER = ",";
    private static final String DSL_PAREN_CLOSE = ")";

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
}
