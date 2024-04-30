package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;

import java.util.List;

public class DSLQueryNodeSourceSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryNodeSourceSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryNodeSourceSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryNodeSourceSelector withCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSourceSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryNodeSourceSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLQueryNodeSourceSelector withoutCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSourceSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLQueryNodeSourceSelector withType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(vertexType));
        return this;
    }

    public DSLQueryNodeSourceSelector withoutType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(vertexType, true));
        return this;
    }

    public DSLQueryDataSourceSelector ofData() {
        return new DSLQueryDataSourceSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
