package org.dataflowanalysis.analysis.dsl.variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a constraint variable with its possible values
 */
public class ConstraintVariable {
    public static final String CONSTANT_NAME = "constant";

    private final String name;
    private Optional<List<String>> possibleValues;

    /**
     * Creates a new constraint variable with the given name and possible values
     * @param name Name of the constraint variable
     * @param possibleValues Possible values of the constraint variable
     */
    public ConstraintVariable(String name, List<String> possibleValues) {
        this.name = name;
        this.possibleValues = Optional.of(possibleValues);
    }

    /**
     * Creates a new constraint variable with the given name and optional possible values
     * @param name Name of the constraint variable
     * @param possibleValues Optional possible values of the constraint variable
     */
    public ConstraintVariable(String name, Optional<List<String>> possibleValues) {
        this.name = name;
        this.possibleValues = possibleValues;
    }

    /**
     * Create a new reference to the constraint variable with the given name
     * @param name Given name of the constraint variable
     * @return Returns a new constraint variable reference with the given name
     */
    public static ConstraintVariableReference of(String name) {
        return ConstraintVariableReference.of(name);
    }

    /**
     * Determines whether the constraint variable is a constant
     * @return  Returns true, if the constraint variable is constant.
     *          Otherwise, the method returns false
     */
    public boolean isConstant() {
        return this.name.equals(CONSTANT_NAME);
    }

    /**
     * Determines whether the constraint variable had values assigned
     * @return  Returns true, if the constraint variable has values assigned.
     *          Otherwise, the method returns false
     */
    public boolean hasValues() {
        return this.possibleValues.isPresent();
    }

    /**
     * Sets the possible values of the constraint variable
     * @param possibleValues List of values of the constraint variable
     */
    public void setPossibleValues(List<String> possibleValues) {
        this.possibleValues = Optional.of(possibleValues);
    }

    /**
     * Adds the list of possible values to the constraint variable
     * @param possibleValues List of possible values of the constraint variable
     */
    public void addPossibleValues(List<String> possibleValues) {
        if (this.isConstant()) {
            throw new IllegalStateException();
        }
        if (this.possibleValues.isEmpty()) {
        	this.possibleValues = Optional.of(new ArrayList<>());
        }
        this.possibleValues.get().addAll(possibleValues);
    }

    /**
     * Returns the name of the constraint variable
     * @return Returns the name of the constraint variable
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the assigned values of the constraint variable
     * @return  Returns an optional containing possible values of the constraint variable.
     *          If none have been set, the method returns an empty optional
     */
    public Optional<List<String>> getPossibleValues() {
        return possibleValues;
    }

    @Override
    public String toString() {
        return this.name + " = {" + this.possibleValues.orElse(List.of("undefined")) + "}";
    }
}
