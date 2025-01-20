package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.UUID;

public class DummyCharacteristicValue implements CharacteristicValue {
    private final String characteristicType;
    private final String characteristicValue;

    public DummyCharacteristicValue(String characteristicType, String characteristicValue) {
        this.characteristicType = characteristicType;
        this.characteristicValue = characteristicValue;
    }

    public static DummyCharacteristicValue fromString(String characteristic) {
        String[] split = characteristic.split("\\.");
        if (split.length != 2) {
            throw new IllegalArgumentException("Illegal characteristic string!");
        }
        return new DummyCharacteristicValue(split[0].trim(), split[1].trim());
    }

    @Override
    public String getTypeName() {
        return this.characteristicType;
    }

    @Override
    public String getValueName() {
        return this.characteristicValue;
    }

    @Override
    public String getValueId() {
        return UUID.randomUUID().toString();
    }
}
