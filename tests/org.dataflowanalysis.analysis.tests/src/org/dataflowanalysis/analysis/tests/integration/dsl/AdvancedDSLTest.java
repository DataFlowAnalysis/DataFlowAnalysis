package org.dataflowanalysis.analysis.tests.integration.dsl;

import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.stream.Stream;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;
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

    @Test
    public void testVertexToVertexWithStandardModel() {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint
                .fromString(new StringView("* default: vertex Location.EU neverFlows vertex Location.nonEU"));
        if (constraint.failed()) {
            fail(constraint.getError());
        }

        final var dataFlowDiagramPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.dataflowdiagram");
        final var dataDictionaryPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.datadictionary");

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString())
                .useDataDictionary(dataDictionaryPath.toString())
                .build();
        analysis.initializeAnalysis();
        var flowGraphCollection = analysis.findFlowGraphs();
        flowGraphCollection.evaluate();

        LoggerManager.getInstance()
                .setLevel(Level.TRACE);
        var violations = constraint.getResult()
                .findViolations(flowGraphCollection);
        assertEquals(2, violations.size());
    }

    @Test
    public void testDataToDataWithStandardModel() {
        ParseResult<? extends AnalysisConstraint> constraint = AnalysisConstraint
                .fromString(new StringView("* default: data Sensitivity.Public neverFlows data Encryption.Encrypted"));
        if (constraint.failed()) {
            fail(constraint.getError());
        }

        final var dataFlowDiagramPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.dataflowdiagram");
        final var dataDictionaryPath = Paths.get("scenarios", "dfd", "OnlineShop", "default.datadictionary");

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString())
                .useDataDictionary(dataDictionaryPath.toString())
                .build();
        analysis.initializeAnalysis();
        var flowGraphCollection = analysis.findFlowGraphs();
        flowGraphCollection.evaluate();

        LoggerManager.getInstance()
                .setLevel(Level.TRACE);
        var violations = constraint.getResult()
                .findViolations(flowGraphCollection);
        assertEquals(2, violations.size());
    }
}
