package org.dataflowanalysis.analysis.tests.integration.dsl.advanced;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests whether the advanced {@link AnalysisConstraint} DSL successfully parses valid constraints and rejects invalid
 * ones.
 */
public class AdvancedDSLParsingTest {
    public static Stream<Arguments> correctAdvancedDSL() {
        return Stream.of(Arguments.of("* test1: data A.B flows vertex C.D"),
                Arguments.of("* test2: data A.B alwaysFlows vertex C.D"),
                Arguments.of("* test3: data A.B notAlwaysFlows vertex C.D"),
                Arguments.of("* test4: vertex A.B flows vertex C.D"),
                Arguments.of("* test5: vertex A.B alwaysFlows vertex C.D"),
                Arguments.of("* test6: vertex A.B notAlwaysFlows vertex C.D"),

                Arguments.of("* test7: vertex A.B or vertex C.D neverFlows vertex E.F"),
                Arguments.of("* test8: vertex A.B xor vertex C.D neverFlows vertex E.F"),
                Arguments.of("* test9: not vertex A.B neverFlows vertex C.D"),
                Arguments.of("* test10: vertex A.B neverFlows not vertex C.D"),
                Arguments.of("* test11: vertex A.B neverFlows vertex C.D or vertex E.F"),
                Arguments.of("* test12: vertex A.B neverFlows vertex C.D xor vertex E.F"),
                Arguments.of("* test13: (vertex A.B xor vertex C.D) and not vertex E.F neverFlows any"),

                Arguments.of("* test14: data A.B neverFlows vertex C.D where present $x"),
                Arguments.of("* test15: data A.B neverFlows vertex C.D where present !$x"),
                Arguments.of("* test16: any neverFlows vertex C.D"),
                Arguments.of("* test17: any neverFlows vertex Type.Internal and not vertex Type.Logging"),
                Arguments.of("* test18: vertex A.B neverFlows any"),

                Arguments.of("* test19: data A.B neverFlows data C.D"),
                Arguments.of("* test20: vertex A.B neverFlows vertex C.D"),
                Arguments.of("* test21: data A.B flows vertex C.D"),
                Arguments.of("* test22: data A.B alwaysFlows vertex C.D"),
                Arguments.of("* test23: data A.B notAlwaysFlows vertex C.D"),

                // data with A.B neverFlow to any node with C.D (across TFGs)
                Arguments.of("* test24: data A.B neverFlows any"),
                Arguments.of("* test25: data A.B neverFlows data C.D"),
                Arguments.of("* test26: vertex A.B neverFlows vertex C.D"),
                // data from vertex A.B neverFlows data C.D (across TFGs)
                Arguments.of("* test27: vertex A.B neverFlows data C.D"),

                Arguments.of("* test28: data name contains Test neverFlows vertex C.D"),
                Arguments.of("* test29: data name Test neverFlows vertex C.D"),
                Arguments.of("* test30: data A.B neverFlows vertex name Test"),

                Arguments.of("* test31: data A.B and data C.D neverFlows vertex name Test"),
                Arguments.of("* test32: data A.B and vertex C.D neverFlows vertex name Test"),
                Arguments.of("* test33: (data A.B or vertex C.D) and data E.F neverFlows vertex name Test"),

                Arguments.of("* test34: data A.B neverFlows vertex name contains Test"));
    }

    public static Stream<Arguments> incorrectAdvancedDSL() {
        return Stream.of(Arguments.of("* test data A.B neverFlows vertex C.D"), Arguments.of("* test: data A.B"),
                Arguments.of("* test: data A.B doesFlow vertex C.D"),
                Arguments.of("* test: data A.B neverFlows vertex C.D where present"),
                Arguments.of("* test: data A.B neverFlows vertex C.D extraGarbage"));
    }

    @ParameterizedTest
    @MethodSource("correctAdvancedDSL")
    public void shouldParseCorrectly(String dslString) {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint.fromString(new StringView(dslString));
        if (constraint.failed()) {
            fail(constraint.getError());
        }
        assertTrue(constraint.successful());
        assertEquals(dslString, constraint.toString());
    }

    @ParameterizedTest
    @MethodSource("incorrectAdvancedDSL")
    public void shouldNotParse(String dslString) {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint.fromString(new StringView(dslString));
        assertTrue(constraint.failed());
    }
}
