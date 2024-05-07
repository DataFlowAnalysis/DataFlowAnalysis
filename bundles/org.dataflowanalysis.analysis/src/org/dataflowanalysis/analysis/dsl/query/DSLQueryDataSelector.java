package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;

import java.util.List;

public class DSLQueryDataSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryDataSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLQueryNodeSelector ofNode() {
        return new DSLQueryNodeSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
