package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;

import java.util.List;

public class DSLQueryNodeSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryNodeSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLQueryNodeSelector withType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(vertexType));
        return this;
    }

    public DSLQueryNodeSelector withoutType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(vertexType, true));
        return this;
    }

    public DSLQueryDataSelector ofData() {
        return new DSLQueryDataSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
