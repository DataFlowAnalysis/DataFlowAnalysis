package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CharacteristicsSelectorDataTest {

    @ParameterizedTest
    @MethodSource("correctCharacteristicSelectors")
    public void shouldParseCorrectly(String variableReference, String characteristicType, String characteristicValue) {
        ParseResult<CharacteristicsSelectorData> characteristicsSelectorData = CharacteristicsSelectorData
                .fromString(new StringView(variableReference));
        assertTrue(characteristicsSelectorData.successful());
        assertTrue(characteristicsSelectorData.getResult()
                .characteristicType()
                .values()
                .isPresent());
        assertTrue(characteristicsSelectorData.getResult()
                .characteristicValue()
                .values()
                .isPresent());
        assertEquals(characteristicType, characteristicsSelectorData.getResult()
                .characteristicType()
                .values()
                .get()
                .get(0));
        assertEquals(characteristicValue, characteristicsSelectorData.getResult()
                .characteristicValue()
                .values()
                .get()
                .get(0));
    }

    @ParameterizedTest
    @MethodSource("incorrectCharacteristicSelectors")
    public void shouldNotParse(String variableReference) {
        ParseResult<CharacteristicsSelectorData> characteristicsSelectorData = CharacteristicsSelectorData
                .fromString(new StringView(variableReference));
        assertTrue(characteristicsSelectorData.failed());
    }

    private static Stream<Arguments> correctCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B", "A", "B"), Arguments.of("otherA.otherB", "otherA", "otherB"),
                Arguments.of("utf-8Ä.utf-8Ö", "utf-8Ä", "utf-8Ö"));
    }

    private static Stream<Arguments> incorrectCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("A."), Arguments.of("justSomeText"), Arguments.of(""), Arguments.of("!"));
    }
}
