package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

public class DSLSinkSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLSinkSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeSinkSelector toNode() {
        return new DSLNodeSinkSelector(this.analysisConstraint);
    }
}
