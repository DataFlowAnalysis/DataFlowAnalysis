package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

public class DSLNodeDestinationSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLNodeDestinationSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addFlowDestination(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowDestination(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))))));
        return this;
    }

    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowDestination(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true));
        return this;
    }

    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowDestination(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true)));
        return this;
    }

    public DSLConditionDefinition where() {
        return new DSLConditionDefinition(analysisConstraint);
    }

    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
