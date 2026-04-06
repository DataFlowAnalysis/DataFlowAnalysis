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
    public void shouldParseCorrectly(String vertexNameSelectorString, String expectedVertexName, boolean expectEmpty) {
        StringView string = new StringView(vertexNameSelectorString);
        ParseResult<VertexNameSelector> vertexNameSelector = VertexNameSelector.fromString(string, new DSLContext());
        assertTrue(vertexNameSelector.successful());
        assertEquals(expectedVertexName, vertexNameSelector.getResult()
                .getName());
        assertEquals(expectEmpty, string.empty());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexNameSelectors")
    public void shouldNotParse(String vertexNameSelectorString) {
        StringView string = new StringView(vertexNameSelectorString);
        ParseResult<VertexNameSelector> vertexNameSelector = VertexNameSelector.fromString(string, new DSLContext());
        assertTrue(vertexNameSelector.failed());
        assertEquals(0, string.getPosition());
    }

    private static Stream<Arguments> correctVertexNameSelectors() {
        return Stream.of(Arguments.of("name name", "name", true),
                Arguments.of("name otherA.otherB", "otherA.otherB", true),
                Arguments.of("name some string with spaces", "some", false),
                Arguments.of("name contains name", "name", true));
    }

    private static Stream<Arguments> incorrectVertexNameSelectors() {
        return Stream.of(Arguments.of("name"), Arguments.of(""), Arguments.of("name "), Arguments.of("name contains"),
                Arguments.of("name contains "));
    }
}
