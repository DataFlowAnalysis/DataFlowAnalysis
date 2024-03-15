package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;

public class DSLNodeSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLNodeSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeSourceSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public DSLNodeSourceSelector withType(VertexType vertexType) {
        this.analysisConstraint.addFlowSource(new VertexTypeSelector(vertexType));
        return this;
    }

    public DSLNodeSourceSelector withoutType(VertexType vertexType) {
        this.analysisConstraint.addFlowSource(new VertexTypeSelector(vertexType, true));
        return this;
    }

    public DSLDataSourceSelector ofData() {
        return new DSLDataSourceSelector(this.analysisConstraint);
    }

    public DSLSinkSelector neverFlows() {
        return new DSLSinkSelector(this.analysisConstraint);
    }
}
