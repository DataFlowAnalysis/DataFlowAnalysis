package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

public class ConstraintDSL {
    private final AnalysisConstraint analysisConstraint;

    public ConstraintDSL() {
        this.analysisConstraint = new AnalysisConstraint();
    }

    public DSLNodeSourceSelector ofNode() {
        return new DSLNodeSourceSelector(analysisConstraint);
    }

    public DSLDataSourceSelector ofData() {
        return new DSLDataSourceSelector(analysisConstraint);
    }
}
