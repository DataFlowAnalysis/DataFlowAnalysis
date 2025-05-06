package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexCharacteristicSelectorTest {

    @ParameterizedTest
    @MethodSource("correctVertexCharacteristicSelectors")
    public void shouldParseCorrectly(String variableReference, boolean inverted) {
        ParseResult<VertexCharacteristicsSelector> vertexCharacteristicsSelector = VertexCharacteristicsSelector
                .fromString(new StringView(variableReference), new DSLContext());
        assertTrue(vertexCharacteristicsSelector.successful());
        assertEquals(inverted, vertexCharacteristicsSelector.getResult()
                .isInverted());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexCharacteristicSelectors")
    public void shouldNotParse(String variableReference) {
        ParseResult<VertexCharacteristicsSelector> vertexCharacteristicsSelector = VertexCharacteristicsSelector
                .fromString(new StringView(variableReference), new DSLContext());
        assertTrue(vertexCharacteristicsSelector.failed());
    }

    private static Stream<Arguments> correctVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B", false), Arguments.of("otherA.otherB", false), Arguments.of("!invertedA.invertedB", true));
    }

    private static Stream<Arguments> incorrectVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"), Arguments.of("!."));
    }
}
