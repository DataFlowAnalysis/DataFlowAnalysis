package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicListSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DataCharacteristicListSelectorTest {

    @ParameterizedTest
    @MethodSource("correctDataCharacteristicSelectors")
    public void shouldParseCorrectly(String dataCharacteristicsSelectorString, boolean inverted) {
        StringView stringView = new StringView(dataCharacteristicsSelectorString);
        ParseResult<DataCharacteristicListSelector> dataCharacteristicsSelector = DataCharacteristicListSelector.fromString(stringView, new DSLContext());
        assertTrue(dataCharacteristicsSelector.successful());
        assertTrue(stringView.empty());
        assertEquals(inverted, dataCharacteristicsSelector.getResult().isInverted());
        assertEquals(dataCharacteristicsSelectorString, dataCharacteristicsSelector.getResult().toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectDataCharacteristicSelectors")
    public void shouldNotParse(String dataCharacteristicsSelectorString) {
        StringView stringView = new StringView(dataCharacteristicsSelectorString);
        ParseResult<DataCharacteristicListSelector> dataCharacteristicsSelector = DataCharacteristicListSelector.fromString(stringView, new DSLContext());
        assertTrue(dataCharacteristicsSelector.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctDataCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B,C.D", false), Arguments.of("otherA.otherB,otherC.otherD", false),
                Arguments.of("!invertedA.invertedB,invertedD.invertedC", true));
    }

    private static Stream<Arguments> incorrectDataCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"), Arguments.of("!."), Arguments.of("A.B,"),
                Arguments.of("A.B,C."), Arguments.of(","));
    }
}
