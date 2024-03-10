package org.dataflowanalysis.analysis.dsl;

public class FirstDSLDataSelector {
    public FirstDSLDataSelector withLabel(String characteristicType, String characteristicValue) {
        return this;
    }

    public FirstDSLDataSelector withoutLabel(String characteristicType, String characteristicValue) {
        return this;
    }

    public FirstDSLNodeSelector ofNode() {
        return new FirstDSLNodeSelector();
    }

    public SecondDSLSelector neverFlows() {
        return new SecondDSLSelector();
    }
}
