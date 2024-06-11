package org.dataflowanalysis.analysis.dsl.variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConstraintVariable {
    public static final String CONSTANT_NAME = "constant";

    private final String name;
    private Optional<List<String>> possibleValues;

    public ConstraintVariable(String name, List<String> possibleValues) {
        this.name = name;
        this.possibleValues = Optional.of(possibleValues);
    }

    public ConstraintVariable(String name, Optional<List<String>> possibleValues) {
        this.name = name;
        this.possibleValues = possibleValues;
    }

    public static ConstraintVariableReference of(String name) {
        return ConstraintVariableReference.of(name);
    }

    public boolean isConstant() {
        return this.name.equals(CONSTANT_NAME);
    }

    public boolean hasValues() {
        return this.possibleValues.isPresent();
    }

    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = Optional.of(possibleValues);
    }

    public void addPossibleValues(List<String> possibleValues) {
        if (this.isConstant()) {
            throw new IllegalStateException();
        }
        if (this.possibleValues.isEmpty()) {
        	this.possibleValues = Optional.of(new ArrayList<>());
        }
        this.possibleValues.get().addAll(possibleValues);
    }

    public String getName() {
        return name;
    }

    public Optional<List<String>> getPossibleValues() {
        return possibleValues;
    }
}
