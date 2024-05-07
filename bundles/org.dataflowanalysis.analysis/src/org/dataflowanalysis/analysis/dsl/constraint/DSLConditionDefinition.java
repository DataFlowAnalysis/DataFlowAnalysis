package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.Intersection;
import org.dataflowanalysis.analysis.dsl.selectors.IntersectionConditionalSelector;

public class DSLConditionDefinition {
    private final AnalysisConstraint analysisConstraint;

    public DSLConditionDefinition(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLConditionDefinition isEmpty(Intersection intersection) {
        this.analysisConstraint.addConditionalSelector(new IntersectionConditionalSelector(intersection));
        return this;
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
