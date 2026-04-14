package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.logic.LogicalOperator;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
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
        ParseResult<? extends AbstractSelector> vertexCharacteristicsSelector = LogicalOperator.fromString(string,
                new DSLContext(), true);
        if (vertexCharacteristicsSelector.failed()) {
            fail(vertexCharacteristicsSelector.getError());
        }
        assertTrue(string.empty());
    }

    @ParameterizedTest
    @MethodSource("incorrectLogicOperators")
    public void shouldNotParse(String variableReference) {
        StringView string = new StringView(variableReference);
        ParseResult<? extends AbstractSelector> vertexCharacteristicsSelector = LogicalOperator.fromString(string,
                new DSLContext(), true);
        assertTrue(vertexCharacteristicsSelector.failed());
        assertEquals(0, string.getPosition());
    }

    private static Stream<Arguments> correctLogicOperators() {
        return Stream.of(Arguments.of("A.B"), Arguments.of("A.B | C.D"), Arguments.of("!A.B ^ C.D"),
                Arguments.of("!A.B | C.D & E.F"), Arguments.of("!A.B | (C.D & E.F)"));
    }

    private static Stream<Arguments> incorrectLogicOperators() {
        return Stream.of(Arguments.of(".B &"), Arguments.of("!.B"), Arguments.of("A."), Arguments.of("!"),
                Arguments.of("!. |"));
    }
}
