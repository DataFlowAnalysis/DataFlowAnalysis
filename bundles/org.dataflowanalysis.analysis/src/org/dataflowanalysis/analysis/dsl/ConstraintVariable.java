package org.dataflowanalysis.analysis.dsl;

import java.util.List;

public class ConstraintVariable {
    private final String name;
    private final List<String> possibleValues;

    // TODO: How should evaluating variables work? Either in Selectors or the Constraint?

    public ConstraintVariable(String name, List<String> possibleValues) {
        this.name = name;
        this.possibleValues = possibleValues;
    }
}
