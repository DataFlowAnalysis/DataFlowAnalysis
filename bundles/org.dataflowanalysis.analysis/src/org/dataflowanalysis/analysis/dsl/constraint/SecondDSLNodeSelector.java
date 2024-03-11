package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;

public class SecondDSLNodeSelector {
    private final AnalysisConstraint analysisConstraint;

    public SecondDSLNodeSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public SecondDSLNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public SecondDSLNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
