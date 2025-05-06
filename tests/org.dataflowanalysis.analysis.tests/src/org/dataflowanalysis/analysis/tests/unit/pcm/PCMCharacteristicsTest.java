package org.dataflowanalysis.analysis.tests.unit.pcm;

import org.dataflowanalysis.analysis.pcm.core.PCMCharacteristicValue;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.DataDictionaryCharacterizedFactory;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PCMCharacteristicsTest {
    private static Stream<Arguments> getValidPCMCharacteristics() {
        return Stream.of(
                Arguments.of("Location", "EU", "Location.EU"),
                Arguments.of("Location", "nonEU", "Location.nonEU"),
                Arguments.of("Encrypted", "true", "Encrypted.true")
        );
    }

    private static Stream<Arguments> getInvalidPCMCharacteristics() {
        return Stream.of(
                Arguments.of("Location", ""),
                Arguments.of("", "nonEU"),
                Arguments.of("", ""),
                Arguments.of(null, null),
                Arguments.of("Location", null)
        );
    }

    @ParameterizedTest
    @MethodSource("getValidPCMCharacteristics")
    public void shouldParsePCMCharacteristics(String characteristicTypeName, String characteristicValueName) {
        PCMCharacteristicValue characteristicValue = new PCMCharacteristicValue(this.getCharacteristicType(characteristicTypeName), this.getLiteral(characteristicValueName));
        assertEquals(characteristicTypeName, characteristicValue.getTypeName(), "Characteristic Type should parse correctly");
        assertEquals(characteristicValueName, characteristicValue.getValueName(), "Characteristic Value should parse correctly");
    }

    @ParameterizedTest
    @MethodSource("getInvalidPCMCharacteristics")
    public void shouldNotParsePCMCharacteristics(String characteristicTypeName, String characteristicValueName) {
        assertThrows(IllegalArgumentException.class, () -> new PCMCharacteristicValue(this.getCharacteristicType(characteristicTypeName), this.getLiteral(characteristicValueName)), "Parsing of the characteristic should fail");
    }

    @ParameterizedTest
    @MethodSource("getValidPCMCharacteristics")
    public void shouldDisplayCorrectly(String characteristicTypeName, String characteristicValueName, String expectedDisplayName) {
        PCMCharacteristicValue characteristicValue = new PCMCharacteristicValue(this.getCharacteristicType(characteristicTypeName), this.getLiteral(characteristicValueName));
        assertEquals(expectedDisplayName, characteristicValue.toString(), "Characteristic toString() method should return the correct representation");
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
