package org.dataflowanalysis.analysis.dsl.query;

import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

public class DSLQueryDataSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryDataSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(analysisQuery.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(analysisQuery.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource( new DataCharacteristicsSelector(analysisQuery.getContext(),new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(analysisQuery.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true));
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisQuery.addFlowSource(new DataCharacteristicsSelector(analysisQuery.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true)));
        return this;
    }

    public DSLQueryNodeSelector ofNode() {
        return new DSLQueryNodeSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
