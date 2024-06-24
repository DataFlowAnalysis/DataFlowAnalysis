package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

public class Intersection {
    private final ConstraintVariableReference firstVariable;
    private final ConstraintVariableReference secondVariable;

    public Intersection(ConstraintVariableReference firstVariable, ConstraintVariableReference secondVariable) {
        this.firstVariable = firstVariable;
        this.secondVariable = secondVariable;
    }

    public static Intersection of(ConstraintVariableReference firstVariable, ConstraintVariableReference secondVariable) {
        return new Intersection(firstVariable, secondVariable);
    }

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
}
