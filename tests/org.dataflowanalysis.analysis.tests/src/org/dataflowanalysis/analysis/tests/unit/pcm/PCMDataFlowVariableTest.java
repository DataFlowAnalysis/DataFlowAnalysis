package org.dataflowanalysis.analysis.tests.unit.pcm;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.core.PCMCharacteristicValue;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PCMDataFlowVariableTest {

    private static Stream<Arguments> getValidPCMDataFlowVariables() {
        return Stream.of(
                Arguments.of("ccd", "Location", "EU", "ccd.Location.EU"),
                Arguments.of("query", "Location", "nonEU", "query.Location.nonEU"),
                Arguments.of("RETURN","Encrypted", "true", "RETURN.Encrypted.true")
        );
    }

    private static Stream<Arguments> getInvalidPCMDataFlowVariables() {
        return Stream.of(
                Arguments.of("", "Location", "nonEU"),
                Arguments.of("", "", ""),
                Arguments.of(null, null, null),
                Arguments.of("RETURN", null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("getValidPCMDataFlowVariables")
    public void shouldParse(String variableName, String characteristicType, String characteristicValue) {
        DataCharacteristic dataCharacteristic = new DataCharacteristic(variableName, List.of(new PCMCharacteristicValue(this.getCharacteristicType(characteristicType), this.getLiteral(characteristicValue))));
        assertEquals(variableName, dataCharacteristic.getVariableName(), "Variable name should parse correctly");
        assertEquals(characteristicType, dataCharacteristic.getAllCharacteristics().get(0).getTypeName(), "Characteristic Type should parse correctly");
        assertEquals(characteristicValue, dataCharacteristic.getAllCharacteristics().get(0).getValueName(), "Characteristic Value should parse correctly");
    }

    @ParameterizedTest
    @MethodSource("getInvalidPCMDataFlowVariables")
    public void shouldNotParse(String variableName, String characteristicType, String characteristicValue) {
        assertThrows(IllegalArgumentException.class, () -> new DataCharacteristic(variableName, List.of(new PCMCharacteristicValue(this.getCharacteristicType(characteristicType), this.getLiteral(characteristicValue)))));
    }

    @ParameterizedTest
    @MethodSource("getValidPCMDataFlowVariables")
    public void shouldDisplayCorrectly(String variableName, String characteristicType, String characteristicValue, String expectedDisplayName) {
        DataCharacteristic dataCharacteristic = new DataCharacteristic(variableName, List.of(new PCMCharacteristicValue(this.getCharacteristicType(characteristicType), this.getLiteral(characteristicValue))));
        assertEquals(expectedDisplayName, dataCharacteristic.toString(), "Data Characteristic should display correctly with toString()");
    }

    @ParameterizedTest
    @MethodSource("getValidPCMDataFlowVariables")
    public void shouldFindCorrectly(String variableName, String characteristicType, String characteristicValue) {
        DataCharacteristic dataCharacteristic = new DataCharacteristic(variableName, List.of(new PCMCharacteristicValue(this.getCharacteristicType(characteristicType), this.getLiteral(characteristicValue))));
        List<CharacteristicValue> result = dataCharacteristic.getCharacteristicsWithName(characteristicType);
        assertEquals(1, result.size(), "Should find exactly one characteristic value with type");
        assertEquals(characteristicValue, result.get(0).getValueName(), "Should find correct characteristic value with type");
    }

    private EnumCharacteristicType getCharacteristicType(String characteristicTypeName) {
        EnumCharacteristicType characteristicType = DataDictionaryCharacterizedFactory.eINSTANCE.createEnumCharacteristicType();
        characteristicType.setName(characteristicTypeName);
        return characteristicType;
    }
    private Literal getLiteral(String characteristicValueName) {
        Literal literal = DataDictionaryCharacterizedFactory.eINSTANCE.createLiteral();
        literal.setName(characteristicValueName);
        return literal;
    }
}
