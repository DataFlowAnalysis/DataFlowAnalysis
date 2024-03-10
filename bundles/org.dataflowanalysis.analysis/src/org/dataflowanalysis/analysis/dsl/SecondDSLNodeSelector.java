package org.dataflowanalysis.analysis.dsl;

public class SecondDSLNodeSelector {
    public SecondDSLNodeSelector withLabel(String characteristicType, String characteristicValue) {
        return this;
    }

    public SecondDSLNodeSelector withoutLabel(String characteristicType, String characteristicValue) {
        return this;
    }

    public AnalysisConstraint create() {
        return new AnalysisConstraint();
    }
}
