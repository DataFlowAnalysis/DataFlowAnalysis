package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.selectors.EmptySetOperationConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EmptySetTest {
    @ParameterizedTest
    @MethodSource("correctEmptySetSelectors")
    public void shouldParseCorrectly(String setOperationString, String expectedFirstVariable, String expectedSecondVariable) {
        ParseResult<EmptySetOperationConditionalSelector> emptySetOperationSelector = EmptySetOperationConditionalSelector
                .fromString(new StringView(setOperationString));
        assertTrue(emptySetOperationSelector.successful());
        assertInstanceOf(Intersection.class, emptySetOperationSelector.getResult()
                .getSetOperation());
        Intersection intersection = (Intersection) emptySetOperationSelector.getResult()
                .getSetOperation();
        assertTrue(intersection.getFirstVariable()
                .values()
                .isPresent());
        assertEquals(expectedFirstVariable, intersection.getFirstVariable()
                .values()
                .get()
                .get(0));
        assertTrue(intersection.getSecondVariable()
                .values()
                .isPresent());
        assertEquals(expectedSecondVariable, intersection.getSecondVariable()
                .values()
                .get()
                .get(0));
    }

    @ParameterizedTest
    @MethodSource("incorrectEmptySetSelectors")
    public void shouldNotParse(String setOperationString) {
        ParseResult<EmptySetOperationConditionalSelector> emptySetOperationSelector = EmptySetOperationConditionalSelector
                .fromString(new StringView(setOperationString));
        assertTrue(emptySetOperationSelector.failed());
    }

    private static Stream<Arguments> correctEmptySetSelectors() {
        return Stream.of(Arguments.of("empty intersection(A,B)", "A", "B"), Arguments.of("empty intersection(A,BC)", "A", "BC"));
    }

    private static Stream<Arguments> incorrectEmptySetSelectors() {
        return Stream.of(Arguments.of("empty A"), Arguments.of(""), Arguments.of("empty intersection "));
    }
}
