package org.dataflowanalysis.analysis.tests.dsl;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.VariableConditionalSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VariableConditionalSelectorTest {
    @ParameterizedTest
    @MethodSource("correctVariableConditionalSelectors")
    public void shouldParseCorrectly(String variableNameSelectorString, String expectedVariableName) {
        ParseResult<VariableConditionalSelector> variableConditionalSelector = VariableConditionalSelector.fromString(new StringView(variableNameSelectorString));
        assertTrue(variableConditionalSelector.successful());
        assertTrue(variableConditionalSelector.getResult().getConstraintVariable().values().isPresent());
        assertEquals(expectedVariableName, variableConditionalSelector.getResult().getConstraintVariable().values().get().get(0));
    }

    @ParameterizedTest
    @MethodSource("incorrectVariableConditionalSelectors")
    public void shouldNotParse(String variableNameSelectorString) {
        ParseResult<VariableConditionalSelector> variableConditionalSelector = VariableConditionalSelector.fromString(new StringView(variableNameSelectorString));
        assertTrue(variableConditionalSelector.failed());
    }

    private static Stream<Arguments> correctVariableConditionalSelectors() {
        return Stream.of(
                Arguments.of("present name", "name"),
                Arguments.of("present otherA.otherB", "otherA"),
                Arguments.of("present some string with spaces", "some"),
                Arguments.of("present !otherName", "otherName")
        );
    }

    private static Stream<Arguments> incorrectVariableConditionalSelectors() {
        return Stream.of(
                Arguments.of("present"),
                Arguments.of(""),
                Arguments.of("present "),
                Arguments.of("present !")
        );
    }
}
