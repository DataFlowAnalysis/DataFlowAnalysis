package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;

/**
 * Represents the top level DSL object to constrain destination vertices of a constraint
 */
public class DSLDestinationSelector {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Creates a new DSL destination selector with the given analysis constraint
     * @param analysisConstraint Given analysis constraint
     */
    public DSLDestinationSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    /**
     * Constrains attributes of the destination vertex of the constraint
     * @return Returns destination node selector DSL object
     */
    public DSLNodeDestinationSelector toVertex() {
        return new DSLNodeDestinationSelector(this.analysisConstraint);
    }
}
