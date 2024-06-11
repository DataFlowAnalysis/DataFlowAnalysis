package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.DSLContext;
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
    public boolean matchesCharacteristic(DSLContext context, AbstractVertex<?> vertex, CharacteristicValue characteristic) {
        var characteristicTypeVariable = context.getMapping(vertex, this.characteristicType());
        var characteristicValueVariable = context.getMapping(vertex, this.characteristicValue());

        if (characteristicTypeVariable.hasValues() && !characteristicTypeVariable.getPossibleValues().get().contains(characteristic.getTypeName())) {
            return false;
        }
        if (characteristicValueVariable.hasValues() && !characteristicValueVariable.getPossibleValues().get().contains(characteristic.getValueName())) {
            return false;
        }

        if (!characteristicTypeVariable.isConstant()) {
            characteristicTypeVariable.addPossibleValues(List.of(characteristic.getTypeName()));
        }
        if(!characteristicValueVariable.isConstant()) {
            characteristicValueVariable.addPossibleValues(List.of(characteristic.getValueName()));
        }
        return true;
    }
}
