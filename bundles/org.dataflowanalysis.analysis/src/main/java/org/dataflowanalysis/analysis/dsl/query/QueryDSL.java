package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisQuery;

public class QueryDSL {
    private final AnalysisQuery analysisQuery;

    public QueryDSL() {
        this.analysisQuery = new AnalysisQuery();
    }

    public DSLQueryNodeSelector ofNode() {
        return new DSLQueryNodeSelector(analysisQuery);
    }

    public DSLQueryDataSelector ofData() {
        return new DSLQueryDataSelector(analysisQuery);
    }
}
