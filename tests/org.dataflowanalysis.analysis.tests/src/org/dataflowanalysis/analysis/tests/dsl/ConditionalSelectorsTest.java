package org.dataflowanalysis.analysis.tests.dsl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConditionalSelectorsTest {
    @ParameterizedTest
    @MethodSource("correctConditionalSelectors")
    public void shouldParseCorrectly(String conditionalSelectorString) {
        ParseResult<ConditionalSelectors> conditionalSelectors = ConditionalSelectors.fromString(new StringView(conditionalSelectorString),
                new DSLContext());
        assertTrue(conditionalSelectors.successful());
    }

    @ParameterizedTest
    @MethodSource("incorrectConditionalSelectors")
    public void shouldNotParse(String conditionalSelectorString) {
        ParseResult<ConditionalSelectors> conditionalSelectors = ConditionalSelectors.fromString(new StringView(conditionalSelectorString),
                new DSLContext());
        assertTrue(conditionalSelectors.failed());
    }

    private static Stream<Arguments> correctConditionalSelectors() {
        return Stream.of(Arguments.of("where present A"), Arguments.of("where present A present B"),
                Arguments.of("where present A empty intersection(C,D)"), Arguments.of("where empty intersection(C,D) present A"));
    }

    private static Stream<Arguments> incorrectConditionalSelectors() {
        return Stream.of(Arguments.of("where A"), Arguments.of(""), Arguments.of("where"), Arguments.of("where present intersection(A,B)"),
                Arguments.of("where empty present A"));
    }
}
