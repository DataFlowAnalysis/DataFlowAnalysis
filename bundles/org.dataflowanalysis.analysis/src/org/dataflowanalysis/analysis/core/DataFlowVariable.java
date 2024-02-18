package org.dataflowanalysis.analysis.core;

import java.util.List;
import java.util.stream.Stream;

/**
 * This class represents a data flow variable with a given name and a list of {@link CharacteristicValue}s
 * An element can be represented as such:
 * {@code <variableName>.<characterisicType>.<characteristicValue> }
 */
public record DataFlowVariable(String variableName, List<CharacteristicValue> characteristics) {

    /**
     * Constructs a data flow variable with a given name and an empty list of characteristic values 
     * @param variableName Name of the data flow variable
     */
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
