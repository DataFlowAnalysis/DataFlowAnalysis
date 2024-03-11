package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;

public class FirstDSLNodeSelector {
    private final AnalysisConstraint analysisConstraint;

    public FirstDSLNodeSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public FirstDSLNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public FirstDSLNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public FirstDSLNodeSelector withType(VertexType vertexType) {
        this.analysisConstraint.addFlowSource(new VertexTypeSelector(vertexType));
        return this;
    }

    public FirstDSLNodeSelector withoutType(VertexType vertexType) {
        this.analysisConstraint.addFlowSource(new VertexTypeSelector(vertexType, true));
        return this;
    }

    public FirstDSLDataSelector ofData() {
        return new FirstDSLDataSelector(this.analysisConstraint);
    }

    public SecondDSLSelector neverFlows() {
        return new SecondDSLSelector(this.analysisConstraint);
    }
}
