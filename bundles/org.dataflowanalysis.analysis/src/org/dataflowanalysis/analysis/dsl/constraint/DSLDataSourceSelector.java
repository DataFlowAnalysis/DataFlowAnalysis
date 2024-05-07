package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;

import java.util.List;

public class DSLDataSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    public DSLDataSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue)))));
        return this;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, ConstraintVariable characteristicValueVariable) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLDataSourceSelector withLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))))));
        return this;
    }

    public DSLDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true));
        return this;
    }

    public DSLDataSourceSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(new CharacteristicsSelectorData(new ConstraintVariable("constant", List.of(characteristicType)), new ConstraintVariable("constant", List.of(characteristicValue))), true)));
        return this;
    }

    public DSLNodeSourceSelector ofNode() {
        return new DSLNodeSourceSelector(this.analysisConstraint);
    }

    public DSLDestinationSelector neverFlows() {
        return new DSLDestinationSelector(this.analysisConstraint);
    }
}
