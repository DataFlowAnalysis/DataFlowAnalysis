package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;

import java.util.List;

public class DSLNodeSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLNodeSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeSourceSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLNodeSourceSelector withCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLNodeSourceSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new VertexCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
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

    public DSLDestinationSelector neverFlows() {
        return new DSLDestinationSelector(this.analysisConstraint);
    }
}
