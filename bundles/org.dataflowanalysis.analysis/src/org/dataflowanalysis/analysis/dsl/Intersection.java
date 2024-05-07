package org.dataflowanalysis.analysis.dsl;

public class Intersection {
    private final ConstraintVariable firstVariable;
    private final ConstraintVariable secondVariable;

    public Intersection(ConstraintVariable firstVariable, ConstraintVariable secondVariable) {
        this.firstVariable = firstVariable;
        this.secondVariable = secondVariable;
    }

    public static Intersection of(ConstraintVariable firstVariable, ConstraintVariable secondVariable) {
        return new Intersection(firstVariable, secondVariable);
    }
}
