package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

public class SecondDSLSelector {
    private final AnalysisConstraint analysisConstraint;

    public SecondDSLSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public SecondDSLNodeSelector toNode() {
        return new SecondDSLNodeSelector(this.analysisConstraint);
    }
}
