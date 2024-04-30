package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.IntersectionConditionalSelector;

public class DSLConditionalSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLConditionalSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLConditionalSelector isEmpty(Intersection intersection) {
        this.analysisConstraint.addConditionalSelector(new IntersectionConditionalSelector(intersection));
        return this;
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
