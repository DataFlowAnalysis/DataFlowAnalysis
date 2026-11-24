package org.dataflowanalysis.analysis.dsl.query;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexNameSelector;
import org.dataflowanalysis.analysis.dsl.selectors.VertexType;
import org.dataflowanalysis.analysis.dsl.selectors.VertexTypeSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

public class DSLQueryNodeSelector {
    private final AnalysisQuery analysisQuery;

    public DSLQueryNodeSelector(AnalysisQuery analysisQuery) {
        this.analysisQuery = analysisQuery;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(this.analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues
                .forEach(characteristicValue -> this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(analysisQuery.getContext(),
                        new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                                ConstraintVariableReference.ofConstant(List.of(characteristicValue))))));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                true));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(analysisQuery.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    public DSLQueryNodeSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues
                .forEach(characteristicValue -> this.analysisQuery.addFlowSource(new VertexCharacteristicsSelector(analysisQuery.getContext(),
                        new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                                ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                        true)));
        return this;
    }

    public DSLQueryNodeSelector withType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(analysisQuery.getContext(), vertexType));
        return this;
    }

    public DSLQueryNodeSelector withoutType(VertexType vertexType) {
        this.analysisQuery.addFlowSource(new VertexTypeSelector(analysisQuery.getContext(), vertexType, true));
        return this;
    }

    /**
     * Match vertices that have the given vertex name
     * @param vertexName Name the given vertex should have
     * @return DSL node selector to add more constraints
     */
    public DSLQueryNodeSelector withVertexName(String vertexName) {
        this.analysisQuery.addFlowSource(new VertexNameSelector(vertexName, analysisQuery.getContext()));
        return this;
    }

    /**
     * Match vertices that do not have the given vertex name
     * @param vertexName Name the given vertex should not have
     * @return DSL node selector to add more constraints
     */
    public DSLQueryNodeSelector withoutVertexName(String vertexName) {
        this.analysisQuery.addFlowSource(new VertexNameSelector(vertexName, true, analysisQuery.getContext()));
        return this;
    }

    public DSLQueryDataSelector ofData() {
        return new DSLQueryDataSelector(this.analysisQuery);
    }

    public AnalysisQuery build() {
        return this.analysisQuery;
    }
}
