package org.dataflowanalysis.analysis.tests.dsl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.VertexDestinationSelectors;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexDestinationSelectorTest {
    @ParameterizedTest
    @MethodSource("correctVertexDestinationSelectors")
    public void shouldParseCorrectly(String vertexDestinationSelectorString) {
        ParseResult<VertexDestinationSelectors> vertexDestinationSelectors = VertexDestinationSelectors
                .fromString(new StringView(vertexDestinationSelectorString), new DSLContext());
        assertTrue(vertexDestinationSelectors.successful());
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexDestinationSelectors")
    public void shouldNotParse(String vertexDestinationSelectorString) {
        StringView stringView = new StringView(vertexDestinationSelectorString);
        ParseResult<VertexDestinationSelectors> vertexDestinationSelectors = VertexDestinationSelectors.fromString(stringView, new DSLContext());
        assertTrue(vertexDestinationSelectors.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctVertexDestinationSelectors() {
        return Stream.of(Arguments.of("vertex A.B"), Arguments.of("vertex otherA.otherB"), Arguments.of("vertex A.B C.D"));
    }

    private static Stream<Arguments> incorrectVertexDestinationSelectors() {
        return Stream.of(Arguments.of("vertex A"), Arguments.of(""), Arguments.of("vertex"), Arguments.of("vertex A.B C"));
    }
}
