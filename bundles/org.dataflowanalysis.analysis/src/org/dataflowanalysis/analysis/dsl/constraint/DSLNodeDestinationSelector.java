package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

/**
 * Represents the DSL object of a node destination selector
 */
public class DSLNodeDestinationSelector {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Create a new DSL node destination selector object with the given analysis constraint
     * @param analysisConstraint Given analysis constraint
     */
    public DSLNodeDestinationSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    /**
     * Match vertices that have the given node characteristic
     * @param characteristicType Node characteristic type that must be present at the vertex
     * @param characteristicValue Node characteristic value that must be present at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    /**
     * Match vertices that have the given node characteristic
     * @param characteristicType Node characteristic type that must be present at the vertex
     * @param characteristicValueVariable Node characteristic value variable reference that must be present at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), characteristicValueVariable)));
        return this;
    }

    /**
     * Match vertices that have one of the given node characteristics
     * <p/>
     * Only one node characteristic value must be present at the vertex
     * @param characteristicType Node characteristic type that must be present at the vertex
     * @param characteristicValues Node characteristic value that must be present at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))))));
        return this;
    }

    /**
     * Match vertices that do not have the given node characteristic
     * @param characteristicType Node characteristic type that must be absent at the vertex
     * @param characteristicValue Node characteristic value that must be absent at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true));
        return this;
    }

    /**
     * Match vertices that do not have the given node characteristic
     * <p/>
     * All node characteristic values must be absent at the vertex
     * @param characteristicType Node characteristic type that must be absent at the vertex
     * @param characteristicValues Node characteristic values that must be absent at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addNodeDestinationSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true)));
        return this;
    }

    /**
     * Add constraints on constraint-wide conditions
     * @return Returns DSL condition definition object
     */
    public DSLConditionDefinition where() {
        return new DSLConditionDefinition(analysisConstraint);
    }

    /**
     * Create the analysis constraint from the given DSL definition
     * @return Returns the analysis constrained defined by the DSL
     */
    public AnalysisConstraint create() {
        return this.analysisConstraint;
    }
}
