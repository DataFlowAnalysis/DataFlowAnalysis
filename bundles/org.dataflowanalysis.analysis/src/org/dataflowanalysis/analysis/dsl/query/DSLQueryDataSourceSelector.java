package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;

import java.util.List;

public class DSLQueryDataSourceSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryDataSourceSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryDataSourceSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryDataSourceSelector withLabel(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryDataSourceSelector withLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLQueryDataSourceSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLQueryNodeSourceSelector ofNode() {
        return new DSLQueryNodeSourceSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
