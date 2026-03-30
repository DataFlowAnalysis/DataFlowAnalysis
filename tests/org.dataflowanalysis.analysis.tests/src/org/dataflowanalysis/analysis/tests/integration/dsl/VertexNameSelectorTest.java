package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.VertexNameSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexNameSelectorTest {
    @ParameterizedTest
    @MethodSource("correctVertexNameSelectors")
    public void shouldParseCorrectly(String vertexNameSelectorString, String expectedVertexName) {
        ParseResult<VertexNameSelector> vertexNameSelector = VertexNameSelector.fromString(new StringView(vertexNameSelectorString),
                new DSLContext());
        assertTrue(vertexNameSelector.successful());
        assertEquals(expectedVertexName, vertexNameSelector.getResult()
                .getName());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexNameSelectors")
    public void shouldNotParse(String vertexNameSelectorString) {
        ParseResult<VertexNameSelector> vertexNameSelector = VertexNameSelector.fromString(new StringView(vertexNameSelectorString),
                new DSLContext());
        assertTrue(vertexNameSelector.failed());
    }

    private static Stream<Arguments> correctVertexNameSelectors() {
        return Stream.of(Arguments.of("vertexName name", "name"), Arguments.of("vertexName otherA.otherB", "otherA.otherB"),
                Arguments.of("vertexName some string with spaces", "some"));
    }

    private static Stream<Arguments> incorrectVertexNameSelectors() {
        return Stream.of(Arguments.of("vertexName"), Arguments.of(""), Arguments.of("vertexName "));
    }
}
