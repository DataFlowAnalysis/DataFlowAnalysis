package org.dataflowanalysis.analysis.tests.integration.dsl;

import org.dataflowanalysis.analysis.dsl.SourceSelectors;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SourceSelectorsTest {
    @ParameterizedTest
    @MethodSource("correctSourceSelectors")
    public void shouldParseCorrectly(String sourceSelector) {
        StringView stringView = new StringView(sourceSelector);
        ParseResult<SourceSelectors> sourceSelectors = SourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(sourceSelectors.successful());
        assertTrue(stringView.empty());
        assertEquals(sourceSelector, sourceSelectors.toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectSourceSelectors")
    public void shouldNotParse(String sourceSelector) {
        StringView stringView = new StringView(sourceSelector);
        ParseResult<SourceSelectors> dataCharacteristicsSelector = SourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(dataCharacteristicsSelector.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctSourceSelectors() {
        return Stream.of(
                Arguments.of("vertex A.B"),
                Arguments.of("data A.B"),
                Arguments.of("data A.B vertex A.B"),
                Arguments.of("data otherA.otherB vertex A.B C.D"),
                Arguments.of("data A.B named C vertex A.B C.D"),
                Arguments.of("data A.B,C.D named E vertex otherA.otherB"),
                Arguments.of("data A.B,C.D E.F named G vertex A.B C.D")
        );
    }

    private static Stream<Arguments> incorrectSourceSelectors() {
        return Stream.of(
                Arguments.of("data A"),
                Arguments.of("data A.B vertex A.B C"),
                Arguments.of("data vertex A"),
                Arguments.of("data A.B C.D named E vertex otherA."),
                Arguments.of("data A.B vertex")
        );
    }
}
