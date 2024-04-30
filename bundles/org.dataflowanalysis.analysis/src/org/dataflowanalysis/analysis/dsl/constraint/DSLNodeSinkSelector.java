package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.NodeCharacteristicsSelector;

import java.util.List;

public class DSLNodeSinkSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLNodeSinkSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeSinkSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLNodeSinkSelector withCharacteristic(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLNodeSinkSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLNodeSinkSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLNodeSinkSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowDestination(new NodeCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLConditionalSelector where() {
        return new DSLConditionalSelector(analysisConstraint);
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
