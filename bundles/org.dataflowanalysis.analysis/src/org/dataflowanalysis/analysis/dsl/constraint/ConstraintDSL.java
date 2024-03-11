package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

public class ConstraintDSL {
    private final AnalysisConstraint analysisConstraint;

    /*
    TODO: Missing components:
    - Support variables instead of strings in selectors
    - Allow finer control over data characteristics (e.g. variable name, etc.)
    - Output could generate variable mappings that produced the constraint (e.g. values of the variables causing a violation)
     */

    public ConstraintDSL() {
        this.analysisConstraint = new AnalysisConstraint();
    }

    public FirstDSLNodeSelector ofNode() {
        return new FirstDSLNodeSelector(analysisConstraint);
    }

    public FirstDSLDataSelector ofData() {
        return new FirstDSLDataSelector(analysisConstraint);
    }
}
