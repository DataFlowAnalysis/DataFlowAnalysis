package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.ConstraintVariable;

public record CharacteristicsSelectorData(ConstraintVariable characteristicType, ConstraintVariable characteristicValue) {

    /**
     * Determines whether a characteristic matches the saved reference
     * @param characteristic Provided characteristic that should be matched
     * @return Returns true of the provided characteristic matches the saved reference.
     * Otherwise, the method returns false.
     */
    public boolean matchesCharacteristic(CharacteristicValue characteristic) {
        if (this.characteristicType.getPossibleValues().isEmpty() || this.characteristicValue.getPossibleValues().isEmpty()) {
            throw new IllegalStateException("Possible values of constraint variables are undefined");
        }
        return this.characteristicType.getPossibleValues().get().contains(characteristic.getTypeName()) &&
                this.characteristicValue.getPossibleValues().get().contains(characteristic.getValueName());
    }
}
