package org.dataflowanalysis.analysis.core;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a data characteristic with a given name and a list of {@link CharacteristicValue}s An element
 * can be represented as such: {@code <variableName>.<characteristicType>.<characteristicValue> }
 */
public record DataCharacteristic(String variableName, List<CharacteristicValue> characteristics) {

    /**
     * Constructs a data characteristic with a given name and an empty list of characteristic values
     * @param variableName Name of the data characteristic
     */
    public DataCharacteristic(String variableName) {
        this(variableName, List.of());
    }

    /**
     * Adds a characteristic value to the list of stored characteristics of the data characteristic
     * @param characteristic Characteristic value that is added to the data characteristic
     * @return Returns a new data characteristic object with the updated characteristic values
     */
    public DataCharacteristic addCharacteristic(CharacteristicValue characteristic) {
        List<CharacteristicValue> newCharacteristics = Stream.concat(characteristics.stream(), Stream.of(characteristic))
                .toList();
        return new DataCharacteristic(variableName, newCharacteristics);
    }

    /**
     * Determines, whether the data characteristic has a characteristic value applied. This is determined by
     * {@link Object#equals(Object)}.
     * @param characteristic Characteristic value that is searched
     * @return Returns true, if the data characteristic has the characteristic value applied. Otherwise, the method returns
     * false.
     */
    public boolean hasCharacteristic(CharacteristicValue characteristic) {
        return this.characteristics.contains(characteristic);
    }

    /**
     * Returns a list of all characteristic values that are applied at the data characteristic
     * @return Returns a list of all characteristic values present at the data characterstic
     */
    public List<CharacteristicValue> getAllCharacteristics() {
        return this.characteristics;
    }

    /**
     * Returns a list of characteristic with the given characteristic type
     * @param characteristicType Name of the characteristic type
     * @return Returns a list of all characteristics matching the characteristic type
     */
    public List<CharacteristicValue> getCharacteristicsWithName(String characteristicType) {
        return this.characteristics()
                .stream()
                .filter(cv -> cv.getTypeName()
                        .equals(characteristicType))
                .collect(Collectors.toList());
    }

    /**
     * Returns the name of the data characteristic. For the data characteristic {@code ccd.Sensitivity.Personal}, it will
     * return {@code ccd}
     * @return Returns the name of the data characteristic
     */
    public String getVariableName() {
        return variableName;
    }
}
