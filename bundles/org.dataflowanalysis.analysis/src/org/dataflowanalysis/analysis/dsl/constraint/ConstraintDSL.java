package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

/**
 * Represents a DSL constraint for the data flow analysis
 */
public class ConstraintDSL {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Creates a new constraint DSL type to create an {@link AnalysisConstraint}
     */
    public ConstraintDSL() {
        this.analysisConstraint = new AnalysisConstraint();
    }

    /**
     * Add constraint on originating node
     * @return Returns DSL type to specify node constraints
     */
    public DSLNodeSourceSelector ofNode() {
        return new DSLNodeSourceSelector(analysisConstraint);
    }

    /**
     * Add constraint on the data of the originating node
     * @return Returns DSL type to specify data constraints on node
     */
    public DSLDataSourceSelector ofData() {
        return new DSLDataSourceSelector(analysisConstraint);
    }
}
