package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.core.DataCharacteristic;

public class CharacteristicsFactory {
    private final String variableName;

    public CharacteristicsFactory(String variableName) {
        this.variableName = variableName;
    }

    public static CharacteristicsFactory of(String variableName) {
        return new CharacteristicsFactory(variableName);
    }

    public DataCharacteristic with(String... characteristics) {
        DataCharacteristic dataCharacteristic = new DataCharacteristic(variableName);
        for (String characteristic : characteristics) {
            dataCharacteristic = dataCharacteristic.addCharacteristic(DummyCharacteristicValue.fromString(characteristic));
        }
        return dataCharacteristic;
    }
}
