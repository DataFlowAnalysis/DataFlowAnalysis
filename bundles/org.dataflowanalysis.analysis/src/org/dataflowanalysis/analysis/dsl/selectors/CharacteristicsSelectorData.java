package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.CharacteristicValue;

public record CharacteristicsSelectorData(String characteristicType, String characteristicValue) {

    /**
     * Determines whether a characteristic matches the saved reference
     * @param characteristic Provided characteristic that should be matched
     * @return Returns true of the provided characteristic matches the saved reference.
     * Otherwise, the method returns false.
     */
    public boolean matchesCharacteristic(CharacteristicValue characteristic) {
        return characteristic.getTypeName().equals(this.characteristicType) &&
                characteristic.getValueName().equals(this.characteristicValue);
    }
}
