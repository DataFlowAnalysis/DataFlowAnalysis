package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;

public class DSLDataSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLDataSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue)));
        return this;
    }

    public DSLDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(characteristicType, characteristicValue), true));
        return this;
    }

    public DSLNodeSourceSelector ofNode() {
        return new DSLNodeSourceSelector(this.analysisConstraint);
    }

    public DSLSinkSelector neverFlows() {
        return new DSLSinkSelector(this.analysisConstraint);
    }
}
