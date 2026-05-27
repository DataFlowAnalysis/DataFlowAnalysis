package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.logic.LogicalOperator;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LogicDSLTest {
    @ParameterizedTest
    @MethodSource("correctLogicOperators")
    public void shouldParseCorrectly(String logicOperator) {
        StringView string = new StringView(logicOperator);
        ParseResult<? extends AbstractSelector> selector = LogicalOperator.fromString(string, new DSLContext());
        if (selector.failed()) {
            fail(selector.getError());
        }
        assertTrue(string.empty());
    }

    @ParameterizedTest
    @MethodSource("incorrectLogicOperators")
    public void shouldNotParse(String variableReference) {
        StringView string = new StringView(variableReference);
        ParseResult<? extends AbstractSelector> vertexCharacteristicsSelector = LogicalOperator.fromString(string,
                new DSLContext());
        assertTrue(vertexCharacteristicsSelector.failed());
        assertEquals(0, string.getPosition());
    }

    private static Stream<Arguments> correctLogicOperators() {
        return Stream.of(Arguments.of("vertex A.B"), Arguments.of("vertex A.B or vertex C.D"),
                Arguments.of("vertex !A.B xor vertex C.D"), Arguments.of("vertex !A.B or vertex C.D and vertex E.F"),
                Arguments.of("vertex !A.B or (vertex C.D and vertex E.F)"));
    }

    private static Stream<Arguments> incorrectLogicOperators() {
        return Stream.of(Arguments.of("vertex .B and"), Arguments.of("vertex !.B"), Arguments.of("vertex A."),
                Arguments.of("vertex !"), Arguments.of("vertex !. or"));
    }
}
