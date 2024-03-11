package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;

public class DSLNodeSinkSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLNodeSinkSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeSinkSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public DSLNodeSinkSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
