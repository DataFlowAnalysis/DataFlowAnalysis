package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.ArrayList;
import java.util.List;

public class DSLDataSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLDataSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues.forEach(it -> data.add(new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(it)))));
        this.analysisConstraint.addFlowSource(new DataCharacteristicListSelector(analysisConstraint.getContext(), data));
        return this;
    }

    public DSLDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(),new CharacteristicsSelectorData( ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant( List.of(characteristicValue))), true));
        return this;
    }

    public DSLDataSourceSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true)));
        return this;
    }

    public DSLNodeSourceSelector ofNode() {
        return new DSLNodeSourceSelector(this.analysisConstraint);
    }

    public DSLDestinationSelector neverFlows() {
        return new DSLDestinationSelector(this.analysisConstraint);
    }
}
