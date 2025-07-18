package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.VertexSourceSelectors;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexSourceSelectorTest {
    @ParameterizedTest
    @MethodSource("correctVertexSourceSelectors")
    public void shouldParseCorrectly(String vertexSourceSelectorString) {
        StringView stringView = new StringView(vertexSourceSelectorString);
        ParseResult<VertexSourceSelectors> vertexSourceSelectors = VertexSourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(vertexSourceSelectors.successful());
        assertTrue(stringView.empty());
        assertEquals(vertexSourceSelectorString, vertexSourceSelectors.getResult()
                .toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexSourceSelectors")
    public void shouldNotParse(String vertexSourceSelectorString) {
        StringView stringView = new StringView(vertexSourceSelectorString);
        ParseResult<VertexSourceSelectors> vertexSourceSelectors = VertexSourceSelectors.fromString(stringView, new DSLContext());
        assertTrue(vertexSourceSelectors.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctVertexSourceSelectors() {
        return Stream.of(Arguments.of("vertex A.B A.B,A.C"), Arguments.of("vertex otherA.otherB"), Arguments.of("vertex A.B C.D"),
                Arguments.of("vertex A.B,C.D"));
    }

    private static Stream<Arguments> incorrectVertexSourceSelectors() {
        return Stream.of(Arguments.of("vertex A"), Arguments.of(""), Arguments.of("vertex"), Arguments.of("vertex A.B C"),
                Arguments.of("vertex A.B,C."));
    }
}
