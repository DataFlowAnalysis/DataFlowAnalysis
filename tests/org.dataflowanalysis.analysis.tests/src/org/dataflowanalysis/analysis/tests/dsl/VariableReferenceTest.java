package org.dataflowanalysis.analysis.tests.dsl;

import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariableReference;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class VariableReferenceTest {
    @ParameterizedTest
    @MethodSource("correctVariableReferences")
    public void shouldParseCorrectly(String variableReference, String expectedVariableName) {
        ParseResult<ConstraintVariableReference> constraintVariableReference = ConstraintVariableReference.fromString(new StringView(variableReference));
        assertTrue(constraintVariableReference.successful());
        assertEquals(expectedVariableName, constraintVariableReference.getResult().name());
    }

    @ParameterizedTest
    @MethodSource("incorrectVariableReferences")
    public void shouldNotParse(String variableReference) {
        ParseResult<ConstraintVariableReference> constraintVariableReference = ConstraintVariableReference.fromString(new StringView(variableReference));
        assertTrue(constraintVariableReference.failed());
    }

    private static Stream<Arguments> correctVariableReferences() {
        return Stream.of(
                Arguments.of("$valid", "valid"),
                Arguments.of("$alsoValid1", "alsoValid1"),
                Arguments.of("$someNotAsciiÄ", "someNotAsciiÄ"),
                Arguments.of("someConstant", ConstraintVariable.CONSTANT_NAME),
                Arguments.of("someOtherConstant", ConstraintVariable.CONSTANT_NAME),
                Arguments.of("whatAbout$This?", ConstraintVariable.CONSTANT_NAME)
        );
    }

    private static Stream<Arguments> incorrectVariableReferences() {
        return Stream.of(
                Arguments.of("$ space"),
                Arguments.of("$"),
                Arguments.of("")
        );
    }
}
