package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class AdvancedDSLTest {
    public static Stream<Arguments> correctAdvancedDSL() {
        return Stream.of(Arguments.of("* test1: data A.B neverFlows data C.D"),
                Arguments.of("* test2: vertex A.B neverFlows vertex C.D"),
                Arguments.of("* test3: data A.B flows vertex C.D"),
                Arguments.of("* test4: data A.B alwaysFlows vertex C.D"),
                Arguments.of("* test5: data A.B notAlwaysFlows vertex C.D"),

                Arguments.of("* test6: data A.B neverFlows vertex any"),
                Arguments.of("* test7: data A.B neverFlows data C.D"), // data with A.B neverFlow to any node with C.D
                                                                       // (across TFGs)
                Arguments.of("* test8: vertex A.B neverFlows vertex C.D"),
                Arguments.of("* test9: vertex A.B neverFlows data C.D"), // data from vertex A.B neverFlows data C.D
                                                                         // (across TFGs)

                Arguments.of("* test10: data name contains Test neverFlows vertex C.D"),
                Arguments.of("* test11: data name Test neverFlows vertex C.D"),
                Arguments.of("* test12: data A.B neverFlows vertex name Test"),
                Arguments.of("* test13: data A.B neverFlows vertex name contains Test"));
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
}
