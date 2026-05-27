package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class DataCharacteristicsSelectorTest {

    @ParameterizedTest
    @MethodSource("correctDataCharacteristicSelectors")
    public void shouldParseCorrectly(String variableReference, boolean inverted) {
        ParseResult<DataSelector> selector = DataCharacteristicsSelector.fromString(new StringView(variableReference),
                new DSLContext());
        assertTrue(selector.successful());
        if ((selector.getResult() instanceof DataCharacteristicsSelector dataCharacteristicsSelector)) {
            assertEquals(inverted, dataCharacteristicsSelector.isInverted());
        } else {
            fail("Selector is not a data characteristics selector");
        }
    }

    @ParameterizedTest
    @MethodSource("incorrectDataCharacteristicSelectors")
    public void shouldNotParse(String variableReference) {
        ParseResult<DataSelector> dataCharacteristicsSelector = DataCharacteristicsSelector
                .fromString(new StringView(variableReference), new DSLContext());
        assertTrue(dataCharacteristicsSelector.failed());
    }

    private static Stream<Arguments> correctDataCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B", false), Arguments.of("otherA.otherB", false),
                Arguments.of("!invertedA.invertedB", true));
    }

    private static Stream<Arguments> incorrectDataCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"),
                Arguments.of("!."));
    }
}
