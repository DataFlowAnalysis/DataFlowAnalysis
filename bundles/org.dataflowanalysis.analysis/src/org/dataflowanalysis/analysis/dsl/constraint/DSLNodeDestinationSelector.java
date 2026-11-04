package org.dataflowanalysis.analysis.dsl.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.*;
import org.dataflowanalysis.analysis.dsl.selectors.logic.AndLogicalOperator;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexNameSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexPredicateSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

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
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))));
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
        return this;
    }

    /**
     * Match vertices that have the given node characteristic
     * @param characteristicType Node characteristic type that must be present at the vertex
     * @param characteristicValueVariable Node characteristic value variable reference that must be present at the
     * vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withCharacteristic(String characteristicType,
            ConstraintVariableReference characteristicValueVariable) {
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        characteristicValueVariable));
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
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
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues.forEach(it -> data.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(it)))));
        AbstractSelector selector = new VertexCharacteristicsListSelector(analysisConstraint.getContext(), data);
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
        return this;
    }

    /**
     * Match vertices that do not have the given node characteristic
     * @param characteristicType Node characteristic type that must be absent at the vertex
     * @param characteristicValue Node characteristic value that must be absent at the vertex
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                true);
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
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
    public DSLNodeDestinationSelector withoutCharacteristic(String characteristicType,
            List<String> characteristicValues) {
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues.forEach(it -> data.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(it)))));
        AbstractSelector selector = new VertexCharacteristicsListSelector(analysisConstraint.getContext(), data, true);
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
        return this;
    }

    /**
     * Match vertices that have the given vertex name
     * @param vertexName Name the given vertex should have
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withVertexName(String vertexName) {
        AbstractSelector selector = new VertexNameSelector(vertexName, analysisConstraint.getContext());
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
        return this;
    }

    /**
     * Match vertices that do not have the given vertex name
     * @param vertexName Name the given vertex should not have
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector withoutVertexName(String vertexName) {
        AbstractSelector selector = new VertexNameSelector(vertexName, true, false, analysisConstraint.getContext());
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
        return this;
    }

    /**
     * Match vertices that match the given predicate
     * <p/>
     * <b>Warning: This selector cannot be serialized into a string</b>
     * @param predicate Given predicate the vertices must have
     * @return DSL node selector to add more constraints
     */
    public DSLNodeDestinationSelector with(Predicate<AbstractVertex<?>> predicate) {
        AbstractSelector selector = new VertexPredicateSelector(analysisConstraint.getContext(), predicate);
        if (!(this.analysisConstraint.getDestinationSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getDestinationSelector(),
                    selector, analysisConstraint.getContext());
            this.analysisConstraint.setDestinationSelector(operator);
        } else {
            this.analysisConstraint.setDestinationSelector(selector);
        }
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
