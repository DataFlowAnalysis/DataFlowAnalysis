package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;

import java.util.List;

public class FirstDSLDataSelector {
    private final AnalysisConstraint analysisConstraint;

    public FirstDSLDataSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public FirstDSLDataSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public FirstDSLDataSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public FirstDSLNodeSelector ofNode() {
        return new FirstDSLNodeSelector(this.analysisConstraint);
    }

    public SecondDSLSelector neverFlows() {
        return new SecondDSLSelector(this.analysisConstraint);
    }
}
