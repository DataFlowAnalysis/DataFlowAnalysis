package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.IntersectionConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VariableConditionalSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

/**
 * Represents a DSL constraint builder on constraint-wide conditions
 */
public class DSLConditionDefinition {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Creates a new DSL constraint definition with the given analysis constraint
     * @param analysisConstraint Analysis constraint of the condition definition
     */
    public DSLConditionDefinition(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    /**
     * Match vertices when the constraint variable referenced is empty
     * @param constraintVariable Referenced constraint variable
     * @return Return Condition DSL type for more definitions
     */
    public DSLConditionDefinition isEmpty(ConstraintVariableReference constraintVariable) {
        this.analysisConstraint.addConditionalSelector(new VariableConditionalSelector(constraintVariable, true));
        return this;
    }

    /**
     * Match vertices when the constraint variable referenced is not empty
     * @param constraintVariable Referenced constraint variable
     * @return Return Condition DSL type for more definitions
     */
    public DSLConditionDefinition isNotEmpty(ConstraintVariableReference constraintVariable) {
        this.analysisConstraint.addConditionalSelector(new VariableConditionalSelector(constraintVariable, false));
        return this;
    }

    /**
     * Match vertices when the intersection is empty
     * @param intersection Intersection that is calculate
     * @return Return Condition DSL type for more definitions
     */
    public DSLConditionDefinition isEmpty(Intersection intersection) {
        this.analysisConstraint.addConditionalSelector(new IntersectionConditionalSelector(intersection));
        return this;
    }

    /**
     * Create the analysis constraint from the given DSL definition
     * @return Returns the analysis constrained defined by the DSL
     */
    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
