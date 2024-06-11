package org.dataflowanalysis.analysis.dsl.variable;

import java.util.List;
import java.util.Optional;

public record ConstraintVariableReference (String name, Optional<List<String>> values) {

    public static ConstraintVariableReference of(String name) {
        return new ConstraintVariableReference(name, Optional.empty());
    }

    public static ConstraintVariableReference of(String name, List<String> values) {
        return new ConstraintVariableReference(name, Optional.of(values));
    }

    public static ConstraintVariableReference ofConstant(List<String> values) {
        return new ConstraintVariableReference(ConstraintVariable.CONSTANT_NAME, Optional.of(values));
    }

    public boolean isConstant() {
        return this.name.equals(ConstraintVariable.CONSTANT_NAME);
    }
}
