package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BaseTest {
    private DFDConfidentialityAnalysis analysis;

    @BeforeEach
    public void initAnalysis() {
        final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
        final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");

        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .build();
    }

    private List<? extends AbstractVertex<?>> getViolationsForConstraint(Predicate<? super AbstractVertex<?>> constraint) {
        this.analysis.initializeAnalysis();
        DFDFlowGraphCollection flowGraph = this.analysis.findFlowGraphs();
        flowGraph.evaluate();

        return analysis.queryDataFlow(flowGraph.getTransposeFlowGraphs()
                .get(0), constraint);
    }

    @Test
    public void numberOfTransposeFlowGraphs_equalsFour() {
        this.analysis.initializeAnalysis();
        DFDFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        assertEquals(flowGraph.getTransposeFlowGraphs()
                .size(), 4);
    }

    @Test
    public void noVertexCharacteristics_returnsNoViolation() {
        var results = this.getViolationsForConstraint(node -> node.getAllVertexCharacteristics().isEmpty());
        assertTrue(results.isEmpty());
    }

    @Test
    public void noVertexCharacteristics_returnsViolations() {
        var results = this.getViolationsForConstraint(node -> !node.getAllVertexCharacteristics().isEmpty());
        assertFalse(results.isEmpty());
    }

    @Test
    public void test_unification() {
        this.analysis.initializeAnalysis();
        DFDFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            assertTrue(transposeFlowGraph.getVertices()
                    .stream()
                    .filter(v -> ((DFDVertex) v).getName()
                            .equals("In"))
                    .count() < 2);
        }
    }

    @Test
    public void test_labelPropagation() {
        this.analysis.initializeAnalysis();
        DFDFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : transposeFlowGraph.getVertices()) {
                assertTrue((!vertex.getAllIncomingDataCharacteristics()
                        .isEmpty())
                        || (!vertex.getAllOutgoingDataCharacteristics()
                                .isEmpty()));
            }
        }
    }

}
