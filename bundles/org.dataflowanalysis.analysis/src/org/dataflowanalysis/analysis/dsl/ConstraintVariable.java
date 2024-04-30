package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;

public class ConstraintVariable {
    private final String name;
    private final List<String> possibleValues;

    public ConstraintVariable(String name, List<String> possibleValues) {
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public static ConstraintVariable of(String name) {
        return new ConstraintVariable(name, new ArrayList<>());
    }

    public String getName() {
        return name;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }
}
