package org.dataflowanalysis.analysis.dsl.constraint;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.selectors.*;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

/**
 * Represents the DSL object of a source destination selector
 */
public class DSLNodeSourceSelector {
    private final AnalysisConstraint analysisConstraint;

    /**
     * Creates a new DSL node source selector with the given analysis constraint
     * @param analysisConstraint Given analysis constraint
     */
    public DSLNodeSourceSelector(AnalysisConstraint analysisConstraint) {
        this.analysisConstraint = analysisConstraint;
    }

    /**
     * Match vertices with the given characteristic type and value
     * @param characteristicType Characteristic type that must be present at the vertex
     * @param characteristicValue Characteristic value that must be present at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addNodeSourceSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                false, true));
        return this;
    }

    /**
     * Match vertices with the given characteristic type and value
     * @param characteristicType Characteristic type that must be present at the vertex
     * @param characteristicValueVariable Characteristic value reference that must be present at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addNodeSourceSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable),
                false, true));
        return this;
    }

    /**
     * Match vertices with the given characteristic type and values
     * <p/>
     * Matches a vertex if it has one of the provided characteristic values
     * @param characteristicType Characteristic type that must be present at the vertex
     * @param characteristicValues Characteristic values of which one must be present at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withCharacteristic(String characteristicType, List<String> characteristicValues) {
        List<CharacteristicsSelectorData> data = new ArrayList<>();
        characteristicValues
                .forEach(it -> data.add(new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(it)))));
        this.analysisConstraint.addNodeSourceSelector(new VertexCharacteristicsListSelector(analysisConstraint.getContext(), data));
        return this;
    }

    /**
     * Match vertices without the given characteristic type and value
     * @param characteristicType Characteristic type that must be absent at the vertex
     * @param characteristicValue Characteristic value that must be absent at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, String characteristicValue) {
        this.analysisConstraint.addNodeSourceSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)),
                        ConstraintVariableReference.ofConstant(List.of(characteristicValue))),
                true, true));
        return this;
    }

    // TODO why does this handle non-constant ConstraintVariableReference here but all other classes don't ?
    /**
     * Match vertices without the given characteristic type and value
     * @param characteristicType Characteristic type that must be absent at the vertex
     * @param characteristicValueVariable Characteristic value variable reference that must be absent at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, ConstraintVariableReference characteristicValueVariable) {
        this.analysisConstraint.addNodeSourceSelector(new VertexCharacteristicsSelector(analysisConstraint.getContext(),
                new CharacteristicsSelectorData(ConstraintVariableReference.ofConstant(List.of(characteristicType)), characteristicValueVariable),
                false, true));
        return this;
    }

    /**
     * Match vertices without the given characteristic type and value
     * <p/>
     * Matches a vertex if none of the provided characteristic values are present
     * @param characteristicType Characteristic type that must be absent at the vertex
     * @param characteristicValues Characteristic values that must be absent at the vertex
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withoutCharacteristic(String characteristicType, List<String> characteristicValues) {
    	characteristicValues.forEach(it -> this.withoutCharacteristic(characteristicType, it));
    	return this;
    }

    /**
     * Match vertices with the given type
     * @param vertexType Type of the vertex the vertices must have
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withType(VertexType vertexType) {
        this.analysisConstraint.addNodeSourceSelector(new VertexTypeSelector(analysisConstraint.getContext(), vertexType, false, true));
        return this;
    }

    /**
     * Match vertices without the given type
     * @param vertexType Type of the vertex the vertices must not have
     * @return Returns a dsl node source selector to add more constraints
     */
    public DSLNodeSourceSelector withoutType(VertexType vertexType) {
        this.analysisConstraint.addNodeSourceSelector(new VertexTypeSelector(analysisConstraint.getContext(), vertexType, true, true));
        return this;
    }

    /**
     * Add more constraints on the data of the source vertex of the constraint
     * @return Returns dsl data source selector object
     */
    public DSLDataSourceSelector ofData() {
        return new DSLDataSourceSelector(this.analysisConstraint);
    }

    /**
     * Add constraints on the data of the destination vertex of the constraint
     * @return Returns dsl destination selector object
     */
    public DSLDestinationSelector neverFlows() {
        return new DSLDestinationSelector(this.analysisConstraint);
    }
}
