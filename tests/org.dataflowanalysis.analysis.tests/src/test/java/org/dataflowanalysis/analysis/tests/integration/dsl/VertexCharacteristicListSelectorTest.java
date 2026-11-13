package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexCharacteristicListSelectorTest {
    @ParameterizedTest
    @MethodSource("correctVertexCharacteristicSelectors")
    public void shouldParseCorrectly(String VertexCharacteristicsSelectorString, boolean inverted) {
        StringView stringView = new StringView(VertexCharacteristicsSelectorString);
        ParseResult<VertexCharacteristicsListSelector> VertexCharacteristicsSelector = VertexCharacteristicsListSelector.fromString(stringView,
                new DSLContext());
        assertTrue(VertexCharacteristicsSelector.successful());
        assertTrue(stringView.empty());
        assertEquals(inverted, VertexCharacteristicsSelector.getResult()
                .isInverted());
        assertEquals(VertexCharacteristicsSelectorString, VertexCharacteristicsSelector.getResult()
                .toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexCharacteristicSelectors")
    public void shouldNotParse(String VertexCharacteristicsSelectorString) {
        StringView stringView = new StringView(VertexCharacteristicsSelectorString);
        ParseResult<VertexCharacteristicsListSelector> VertexCharacteristicsSelector = VertexCharacteristicsListSelector.fromString(stringView,
                new DSLContext());
        assertTrue(VertexCharacteristicsSelector.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B,C.D", false), Arguments.of("otherA.otherB,otherC.otherD", false),
                Arguments.of("!invertedA.invertedB,invertedD.invertedC", true));
    }

    private static Stream<Arguments> incorrectVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"), Arguments.of("!."), Arguments.of("A.B,"),
                Arguments.of("A.B,C."), Arguments.of(","));
    }
}
