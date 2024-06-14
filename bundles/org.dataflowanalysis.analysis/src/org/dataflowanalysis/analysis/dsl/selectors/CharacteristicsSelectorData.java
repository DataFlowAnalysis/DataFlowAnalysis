package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.DSLContext;
import org.dataflowanalysis.analysis.dsl.DSLContextKey;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;

import java.util.List;

public record CharacteristicsSelectorData(ConstraintVariableReference characteristicType, ConstraintVariableReference characteristicValue) {

    /**
     * Determines whether a characteristic matches the saved reference
     * @param context DSL Matching context
     * @param characteristic Provided characteristic that should be matched
     * @return Returns true of the provided characteristic matches the saved reference.
     * Otherwise, the method returns false.
     */
    public boolean matchesCharacteristic(DSLContext context, AbstractVertex<?> vertex, CharacteristicValue characteristic, String variableName, List<String> characteristicTypes, List<String> characteristicValues) {
        var characteristicTypeVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicType());
        var characteristicValueVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicValue());

        if (characteristicTypeVariable.hasValues() && !characteristicTypeVariable.getPossibleValues().get().contains(characteristic.getTypeName())) {
            return false;
        }
        if (characteristicValueVariable.hasValues() && !characteristicValueVariable.getPossibleValues().get().contains(characteristic.getValueName())) {
            return false;
        }

        if (!characteristicTypeVariable.isConstant() && !characteristicTypes.contains(characteristic.getTypeName())) {
            characteristicTypes.add(characteristic.getTypeName());
        }
        if(!characteristicValueVariable.isConstant() && !characteristicValues.contains(characteristic.getValueName())) {
            characteristicValues.add(characteristic.getValueName());
        }
        return true;
    }

    public void applyResults(DSLContext context, AbstractVertex<?> vertex, String variableName,  List<String> characteristicTypes, List<String> characteristicValues) {
        var characteristicTypeVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicType());
        var characteristicValueVariable = context.getMapping(DSLContextKey.of(variableName, vertex), this.characteristicValue());

        if (!characteristicTypeVariable.isConstant()) {
            characteristicTypeVariable.addPossibleValues(characteristicTypes);
        }

        if (!characteristicValueVariable.isConstant()) {
            characteristicValueVariable.addPossibleValues(characteristicValues);
        }
    }
}
