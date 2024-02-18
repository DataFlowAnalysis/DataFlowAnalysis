package org.dataflowanalysis.analysis.core;

import java.util.List;
import java.util.stream.Stream;

public record DataFlowVariable(String variableName, List<CharacteristicValue> characteristics) {

    public DataFlowVariable(String variableName) {
        this(variableName, List.of());
    }

    public DataFlowVariable addCharacteristic(CharacteristicValue characteristic) {
        List<CharacteristicValue> newCharacteristics = Stream.concat(characteristics.stream(), Stream.of(characteristic)).toList();
        return new DataFlowVariable(variableName, newCharacteristics);
    }

    public boolean hasCharacteristic(CharacteristicValue characteristic) {
        return this.characteristics.contains(characteristic);
    }

    public List<CharacteristicValue> getAllCharacteristics() {
        return this.characteristics;
    }

    public String getVariableName() {
        return variableName;
    }
}
