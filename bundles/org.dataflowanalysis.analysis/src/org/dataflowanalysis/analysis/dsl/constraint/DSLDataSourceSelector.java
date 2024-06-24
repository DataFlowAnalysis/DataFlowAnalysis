package org.dataflowanalysis.analysis.dsl.constraint;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.ArrayList;
import java.util.List;

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
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue)))));
        return this;
    }

    /**
     * Matches source vertices with the given characteristic type and characteristic value
     * @param characteristicType Characteristic type that must be present at the source vertex
     * @param characteristicValueVariable Characteristic value variable reference that must be present at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withLabel(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable)));
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
        characteristicValues.forEach(it -> data.add(new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(it)))));
        this.analysisConstraint.addFlowSource(new DataCharacteristicListSelector(analysisConstraint.getContext(), data));
        return this;
    }

    /**
     * Matches source vertices without the given characteristic type and characteristic value
     * @param characteristicType Characteristic type that must be absent at the source vertex
     * @param characteristicValue Characteristic value that must be absent at the source vertex
     * @return Returns DSL constraint builder for source vertex data
     */
    public DSLDataSourceSelector withoutLabel(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(),new CharacteristicsSelectorData( ConstraintVariableReference.ofConstant(List.of(characteristicType)), ConstraintVariableReference.ofConstant( List.of(characteristicValue))), true));
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
        characteristicValues.forEach(characteristicValue -> this.analysisConstraint.addFlowSource(new DataCharacteristicsSelector(analysisConstraint.getContext(), new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant( List.of(characteristicType)), ConstraintVariableReference.ofConstant(List.of(characteristicValue))), true)));
        return this;
    }

    /**
     * Returns a DSL node source selector to add constraints on attributes of the source vertices
     * @return Returns DSL node source selector object
     */
    public DSLNodeSourceSelector ofNode() {
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
