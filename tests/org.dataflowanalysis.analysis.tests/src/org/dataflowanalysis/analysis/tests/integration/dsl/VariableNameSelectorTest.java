package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.VariableNameSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class VariableNameSelectorTest {

    @ParameterizedTest
    @MethodSource("correctVariableNameSelectors")
    public void shouldParseCorrectly(String variableNameSelectorString, String expectedVariableName) {
        ParseResult<VariableNameSelector> variableNameSelector = VariableNameSelector.fromString(new StringView(variableNameSelectorString),
                new DSLContext());
        assertTrue(variableNameSelector.successful());
        assertEquals(expectedVariableName, variableNameSelector.getResult()
                .getVariableName());
    }

    @ParameterizedTest
    @MethodSource("incorrectVariableNameSelectors")
    public void shouldNotParse(String variableNameSelectorString) {
        ParseResult<VariableNameSelector> variableNameSelector = VariableNameSelector.fromString(new StringView(variableNameSelectorString),
                new DSLContext());
        assertTrue(variableNameSelector.failed());
    }

    private static Stream<Arguments> correctVariableNameSelectors() {
        return Stream.of(Arguments.of("dataName name", "name"), Arguments.of("dataName otherA.otherB", "otherA.otherB"),
                Arguments.of("dataName some string with spaces", "some"));
    }

    private static Stream<Arguments> incorrectVariableNameSelectors() {
        return Stream.of(Arguments.of("dataName"), Arguments.of(""), Arguments.of("dataName "));
    }
}
