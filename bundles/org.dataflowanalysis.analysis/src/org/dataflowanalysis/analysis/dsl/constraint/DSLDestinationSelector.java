package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

public class DSLDestinationSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLDestinationSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeDestinationSelector toVertex() {
        return new DSLNodeDestinationSelector(this.analysisConstraint);
    }
}
