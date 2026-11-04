package org.dataflowanalysis.analysis.dsl.query;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.logic.AndLogicalOperator;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

public class DSLQueryDataSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryDataSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))));
        if (this.analysisQuery.getDataSources() instanceof AnySelector) {
            this.analysisQuery.setDataSources(selector);
        } else {
            this.analysisQuery.setDataSources(new AndLogicalOperator(this.analysisQuery.getDataSources(), selector,
                    this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType,
            ConstraintVariableReference characteristicValueVariable) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        characteristicValueVariable));
        if (this.analysisQuery.getDataSources() instanceof AnySelector) {
            this.analysisQuery.setDataSources(selector);
        } else {
            this.analysisQuery.setDataSources(new AndLogicalOperator(this.analysisQuery.getDataSources(), selector,
                    this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryDataSelector withLabel(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> characteristics = new ArrayList<>();
        characteristicValues.forEach(characteristicValue -> characteristics.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        AbstractSelector selector = new DataCharacteristicListSelector(new DSLContext(), characteristics);
        if (this.analysisQuery.getDataSources() instanceof AnySelector) {
            this.analysisQuery.setDataSources(selector);
        } else {
            this.analysisQuery.setDataSources(new AndLogicalOperator(this.analysisQuery.getDataSources(), selector,
                    this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))));
        if (this.analysisQuery.getDataSources() instanceof AnySelector) {
            this.analysisQuery.setDataSources(selector);
        } else {
            this.analysisQuery.setDataSources(new AndLogicalOperator(this.analysisQuery.getDataSources(), selector,
                    this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryDataSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> characteristics = new ArrayList<>();
        characteristicValues.forEach(characteristicValue -> characteristics.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        AbstractSelector selector = new DataCharacteristicListSelector(new DSLContext(), characteristics, true);
        if (this.analysisQuery.getDataSources() instanceof AnySelector) {
            this.analysisQuery.setDataSources(selector);
        } else {
            this.analysisQuery.setDataSources(new AndLogicalOperator(this.analysisQuery.getDataSources(), selector,
                    this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector ofNode() {
        return new DSLQueryNodeSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
