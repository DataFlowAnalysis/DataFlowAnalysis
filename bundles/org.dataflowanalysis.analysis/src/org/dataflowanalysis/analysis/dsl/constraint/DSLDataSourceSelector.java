package org.dataflowanalysis.analysis.dsl.constraint;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.logic.AndLogicalOperator;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

/**
 * Represents a DSL constraint builder for the source node data
 */
public class DSLDataSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Creates a new DSL constraint builder for source vertex data with the given analysis constraint
     * @param analysisConstraint Given analysis constraint
     */
    public DSLDataSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    /**
     * Matches source vertices with the given characteristic type and characteristic value
     * @param characteristicType Characteristic type that must be present at the source vertex
     * @param characteristicValue Characteristic value that must be present at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withLabel(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))));
        if (!(this.analysisConstraint.getSourceSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getSourceSelector(), selector,
                    analysisConstraint.getContext());
            this.analysisConstraint.setSourceSelector(operator);
        } else {
            this.analysisConstraint.setSourceSelector(selector);
        }
        return this;
    }

    /**
     * Matches source vertices with the given characteristic type and characteristic value
     * @param characteristicType Characteristic type that must be present at the source vertex
     * @param characteristicValueVariable Characteristic value variable reference that must be present at the source
     * vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withLabel(String characteristicType,
            ConstraintVariableReference characteristicValueVariable) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        characteristicValueVariable));
        if (!(this.analysisConstraint.getSourceSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getSourceSelector(), selector,
                    analysisConstraint.getContext());
            this.analysisConstraint.setSourceSelector(operator);
        } else {
            this.analysisConstraint.setSourceSelector(selector);
        }
        return this;
    }

    /**
     * Matches source vertices with the given characteristic type and characteristic values
     * <p/>
     * Matching vertices are vertices that have <b>one</b> matching characteristic value
     * @param characteristicType Characteristic type that must be present at the source vertex
     * @param characteristicValues List of characteristic values of which one must be present at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withLabel(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues.forEach(it -> data.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(it)))));
        AbstractSelector selector = new DataCharacteristicListSelector(analysisConstraint.getContext(), data);
        if (!(this.analysisConstraint.getSourceSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getSourceSelector(), selector,
                    analysisConstraint.getContext());
            this.analysisConstraint.setSourceSelector(operator);
        } else {
            this.analysisConstraint.setSourceSelector(selector);
        }
        return this;
    }

    /**
     * Matches source vertices without the given characteristic type and characteristic value
     * @param characteristicType Characteristic type that must be absent at the source vertex
     * @param characteristicValue Characteristic value that must be absent at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        AbstractSelector selector = new DataCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                true);
        if (!(this.analysisConstraint.getSourceSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getSourceSelector(), selector,
                    analysisConstraint.getContext());
            this.analysisConstraint.setSourceSelector(operator);
        } else {
            this.analysisConstraint.setSourceSelector(selector);
        }
        return this;
    }

    /**
     * Matches source vertices without the given characteristic type and characteristic value
     * <p/>
     * Matching vertices are vertices that have <b>no</b> matching characteristic value
     * @param characteristicType Characteristic type that must be absent at the source vertex
     * @param characteristicValues Characteristic values of which all must be absent at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withoutLabel(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues.forEach(characteristicValue -> data.add(
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        AbstractSelector selector = new DataCharacteristicListSelector(this.analysisConstraint.getContext(), data,
                true);
        if (!(this.analysisConstraint.getSourceSelector() instanceof AnySelector)) {
            AndLogicalOperator operator = new AndLogicalOperator(this.analysisConstraint.getSourceSelector(), selector,
                    analysisConstraint.getContext());
            this.analysisConstraint.setSourceSelector(operator);
        } else {
            this.analysisConstraint.setSourceSelector(selector);
        }
        return this;
    }

    /**
     * Returns a DSL node source selector to add constraints on attributes of the source vertices
     * @return Returns DSL node source selector object
     */
    public DSLNodeSourceSelector fromNode() {
        return new DSLNodeSourceSelector(this.analysisConstraint);
    }

    /**
     * Returns a DSL destination selector to constrain attributes of the destination vertex
     * @return Returns DSL destination selector object
     */
    public DSLDestinationSelector neverFlows() {
        return new DSLDestinationSelector(this.analysisConstraint);
    }
}
