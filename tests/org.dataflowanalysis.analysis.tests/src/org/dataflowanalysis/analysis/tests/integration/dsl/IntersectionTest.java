package org.dataflowanalysis.analysis.tests.dsl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class IntersectionTest {
    @ParameterizedTest
    @MethodSource("correctIntersectionSelector")
    public void shouldParseCorrectly(String intersectionString, String expectedFirstVariable, String expectedSecondVariable) {
        ParseResult<Intersection> intersectionSelector = Intersection.fromString(new StringView(intersectionString));
        assertTrue(intersectionSelector.successful());
        assertTrue(intersectionSelector.getResult()
                .getFirstVariable()
                .values()
                .isPresent());
        assertEquals(expectedFirstVariable, intersectionSelector.getResult()
                .getFirstVariable()
                .values()
                .get()
                .get(0));
        assertTrue(intersectionSelector.getResult()
                .getSecondVariable()
                .values()
                .isPresent());
        assertEquals(expectedSecondVariable, intersectionSelector.getResult()
                .getSecondVariable()
                .values()
                .get()
                .get(0));
    }

    @ParameterizedTest
    @MethodSource("incorrectIntersectionSelectors")
    public void shouldNotParse(String intersectionString) {
        ParseResult<Intersection> intersectionSelector = Intersection.fromString(new StringView(intersectionString));
        assertTrue(intersectionSelector.failed());
    }

    private static Stream<Arguments> correctIntersectionSelector() {
        return Stream.of(Arguments.of("intersection(A,B)", "A", "B"), Arguments.of("intersection(A,BC)", "A", "BC"));
    }

    private static Stream<Arguments> incorrectIntersectionSelectors() {
        return Stream.of(Arguments.of("intersection(A, BC)"), Arguments.of(""), Arguments.of("intersection "), Arguments.of("intersection(A,)"),
                Arguments.of("intersection(A,B"), Arguments.of("intersection(A,(A)"));
    }
}
