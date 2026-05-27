package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexCharacteristicsListSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexSelector;
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
        ParseResult<VertexSelector> selector = VertexCharacteristicsListSelector.fromString(stringView,
                new DSLContext());
        assertTrue(selector.successful());
        assertTrue(stringView.empty());
        if (selector.getResult() instanceof VertexCharacteristicsListSelector vertexCharacteristicsSelector) {
            assertEquals(inverted, vertexCharacteristicsSelector.isInverted());
            assertEquals("vertex " + VertexCharacteristicsSelectorString, vertexCharacteristicsSelector.toString());
        } else {
            fail("Selector is not a vertex characteristic list selector");
        }
    }

    @ParameterizedTest
    @MethodSource("incorrectVertexCharacteristicSelectors")
    public void shouldNotParse(String VertexCharacteristicsSelectorString) {
        StringView stringView = new StringView(VertexCharacteristicsSelectorString);
        ParseResult<VertexSelector> VertexCharacteristicsSelector = VertexCharacteristicsListSelector
                .fromString(stringView, new DSLContext());
        assertTrue(VertexCharacteristicsSelector.failed() || !stringView.empty());
    }

    private static Stream<Arguments> correctVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of("A.B,C.D", false), Arguments.of("otherA.otherB,otherC.otherD", false),
                Arguments.of("!invertedA.invertedB,invertedD.invertedC", true));
    }

    private static Stream<Arguments> incorrectVertexCharacteristicSelectors() {
        return Stream.of(Arguments.of(".B"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"),
                Arguments.of("!."), Arguments.of("A.B,"), Arguments.of("A.B,C."), Arguments.of(","));
    }
}
