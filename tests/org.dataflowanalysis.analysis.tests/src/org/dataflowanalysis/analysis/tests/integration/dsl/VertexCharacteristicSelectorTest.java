package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VertexCharacteristicSelectorTest {

    @ParameterizedTest
    @MethodSource("correctVertexCharacteristicSelectors")
    public void shouldParseCorrectly(String variableReference, boolean inverted) {
        ParseResult<VertexSelector> selector = VertexCharacteristicsSelector
                .fromString(new StringView(variableReference), new DSLContext());
        assertTrue(selector.successful());
        if (selector.getResult() instanceof VertexCharacteristicsSelector vertexCharacteristicsSelector) {
            assertEquals(inverted, vertexCharacteristicsSelector.isInverted());
        } else {
            fail("Selector is not a vertex characteristic selector");
        }
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexCharacteristicSelectors")
    public void shouldNotParse(String variableReference) {
        ParseResult<VertexSelector> vertexCharacteristicsSelector = VertexCharacteristicsSelector
                .fromString(new StringView(variableReference), new DSLContext());
        assertTrue(vertexCharacteristicsSelector.failed());
    }

    private static Stream<Arguments> correctVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B", false), Arguments.of("otherA.otherB", false),
                Arguments.of("!invertedA.invertedB", true));
    }

    private static Stream<Arguments> incorrectVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"),
                Arguments.of("!."));
    }
}
