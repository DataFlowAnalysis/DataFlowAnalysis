package org.dataflowanalysis.analysis.dsl.query;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.logic.AndLogicalOperator;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexTypeSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

public class DSLQueryNodeSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryNodeSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new VertexCharacteristicsSelector(this.analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))));
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType,
            ConstraintVariableReference characteristicValueVariable) {
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        characteristicValueVariable));
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> vertexCharacteristics = new ArrayList<>();
        characteristicValues.forEach(characteristicValue -> vertexCharacteristics.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        AbstractSelector selector = new VertexCharacteristicsListSelector(this.analysisQuery.getContext(),
                vertexCharacteristics);
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                true);
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType,
            ConstraintVariableReference characteristicValueVariable) {
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        characteristicValueVariable));
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> vertexCharacteristics = new ArrayList<>();
        characteristicValues.forEach(characteristicValue -> vertexCharacteristics.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        AbstractSelector selector = new VertexCharacteristicsListSelector(this.analysisQuery.getContext(),
                vertexCharacteristics, true);
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withType(VertexType vertexType) {
        AbstractSelector selector = new VertexTypeSelector(analysisQuery.getContext(), vertexType);
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryNodeSelector withoutType(VertexType vertexType) {
        AbstractSelector selector = new VertexTypeSelector(analysisQuery.getContext(), vertexType, true);
        if (this.analysisQuery.getVertexDestinations() instanceof AnySelector) {
            this.analysisQuery.setVertexDestinations(selector);
        } else {
            this.analysisQuery.setVertexDestinations(new AndLogicalOperator(this.analysisQuery.getDataSources(),
                    selector, this.analysisQuery.getContext()));
        }
        return this;
    }

    public DSLQueryDataSelector ofData() {
        return new DSLQueryDataSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
