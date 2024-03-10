package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.dsl.selectors.VertexType;

public class FirstDSLNodeSelector {
    public FirstDSLNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        return this;
    }

    public FirstDSLNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        return this;
    }

    public FirstDSLNodeSelector withType(VertexType vertexType) {
        return this;
    }

    public FirstDSLNodeSelector withoutType(VertexType vertexType) {
        return this;
    }

    public FirstDSLDataSelector ofData() {
        return new FirstDSLDataSelector();
    }

    public SecondDSLSelector neverFlows() {
        return new SecondDSLSelector();
    }
}
