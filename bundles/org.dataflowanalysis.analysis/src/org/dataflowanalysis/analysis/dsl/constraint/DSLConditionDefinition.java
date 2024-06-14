package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.IntersectionConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VariableConditionalSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

public class DSLConditionDefinition {
    private final AnalysisConstraint analysisConstraint;

    public DSLConditionDefinition(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLConditionDefinition isEmpty(ConstraintVariableReference constraintVariable) {
        this.analysisConstraint.addConditionalSelector(new VariableConditionalSelector(constraintVariable, true));
        return this;
    }

    public DSLConditionDefinition isNotEmpty(ConstraintVariableReference constraintVariable) {
        this.analysisConstraint.addConditionalSelector(new VariableConditionalSelector(constraintVariable, false));
        return this;
    }

    public DSLConditionDefinition isEmpty(Intersection intersection) {
        this.analysisConstraint.addConditionalSelector(new IntersectionConditionalSelector(intersection));
        return this;
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
