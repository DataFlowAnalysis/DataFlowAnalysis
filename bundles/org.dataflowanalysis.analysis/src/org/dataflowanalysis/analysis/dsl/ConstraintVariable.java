package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstraintVariable {
    private final String name;
    private Optional<List<String>> possibleValues;

    public ConstraintVariable(String name, List<String> possibleValues) {
        this.name = name;
        this.possibleValues = Optional.of(possibleValues);
    }

    public static ConstraintVariable of(String name) {
        return new ConstraintVariable(name, new ArrayList<>());
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = Optional.of(possibleValues);
    }

    public String getName() {
        return name;
    }

    public Optional<List<String>> getPossibleValues() {
        return possibleValues;
    }
}
