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
    public void shouldParseCorrectly(String variableNameSelectorString, String expectedVariableName,
            boolean expectEmpty) {
        StringView string = new StringView(variableNameSelectorString);
        ParseResult<VariableNameSelector> variableNameSelector = VariableNameSelector.fromString(string,
                new DSLContext());
        assertTrue(variableNameSelector.successful());
        assertEquals(expectedVariableName, variableNameSelector.getResult()
                .getVariableName());
        assertEquals(expectEmpty, string.empty());
    }

    @ParameterizedTest
    @MethodSource("incorrectVariableNameSelectors")
    public void shouldNotParse(String variableNameSelectorString) {
        StringView string = new StringView(variableNameSelectorString);
        ParseResult<VariableNameSelector> variableNameSelector = VariableNameSelector.fromString(string,
                new DSLContext());
        assertTrue(variableNameSelector.failed());
        assertEquals(0, string.getPosition());
    }

    private static Stream<Arguments> correctVariableNameSelectors() {
        return Stream.of(Arguments.of("name name", "name", true),
                Arguments.of("name otherA.otherB", "otherA.otherB", true),
                Arguments.of("name some string with spaces", "some", false),
                Arguments.of("name contains test", "test", true));
    }

    private static Stream<Arguments> incorrectVariableNameSelectors() {
        return Stream.of(Arguments.of("name"), Arguments.of(""), Arguments.of("name "), Arguments.of("name contains"),
                Arguments.of("name contains "));
    }
}
