package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;

public class QueryDSL {
    private final AnalysisQuery analysisQuery;

    /*
    TODO: Missing components:
    - Support variables instead of strings in selectors
    - Allow finer control over data characteristics (e.g. variable name, etc.)
    - Output could generate variable mappings that produced the constraint (e.g. values of the variables causing a violation)
     */

    public QueryDSL() {
        this.analysisQuery = new AnalysisQuery();
    }

    public DSLQueryNodeSourceSelector ofNode() {
        return new DSLQueryNodeSourceSelector(analysisQuery);
    }

    public DSLQueryDataSourceSelector ofData() {
        return new DSLQueryDataSourceSelector(analysisQuery);
    }
}
