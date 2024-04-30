package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;

public class QueryDSL {
    private final AnalysisQuery analysisQuery;

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
