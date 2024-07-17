package org.dataflowanalysis.analysis.tests.sequencefinder;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.file.Paths;
import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.dfd.core.DFDCyclicTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnlineShopDFDTest {

    private DFDConfidentialityAnalysis cyclicAnalysis;
    private DFDConfidentialityAnalysis linearAnalysis;

    @BeforeEach
    public void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();

        cyclicAnalysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDCyclicTransposeFlowGraphFinder.class)
                .build();

        linearAnalysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDTransposeFlowGraphFinder.class)
                .build();

        cyclicAnalysis.initializeAnalysis();
        linearAnalysis.initializeAnalysis();
    }

    @Test
    public void testCyclicAnalysis() {
        runAllChecksForAnalysis(cyclicAnalysis);
    }

    @Test
    public void testLinearAnalysis() {
        runAllChecksForAnalysis(linearAnalysis);
    }

    private void runAllChecksForAnalysis(DFDConfidentialityAnalysis analysis) {
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
        checkNumberOfTransposeFlowGraphs(flowGraph, 3);
        checkSinks(flowGraph, List.of("User", "Database", "Database"));
        checkNodeLabels(flowGraph, "User", List.of("EU"));
        checkDataLabelPropagation(flowGraph, "User", List.of("Public"));
        checkRealisticConstraints(analysis,flowGraph, 1, 0);
        checkIsNotCyclic(flowGraph);
    }

    private void checkNumberOfTransposeFlowGraphs(DFDFlowGraphCollection flowGraph, int expectedNumber) {
        assertEquals(flowGraph.getTransposeFlowGraphs()
                .size(), expectedNumber);
    }

    private void checkSinks(DFDFlowGraphCollection flowGraph, List<String> expectedNames) {
        var entityNames = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();

        assertIterableEquals(expectedNames, entityNames);
    }

    private void checkNodeLabels(DFDFlowGraphCollection flowGraph, String vertexName, List<String> expectedLabels) {
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : transposeFlowGraph.getVertices()) {
                if (((DFDVertex) vertex).getName()
                        .equals(vertexName)) {
                    var userVertexLabels = retrieveNodeLabels(vertex);
                    assertIterableEquals(expectedLabels, userVertexLabels);
                    return;
                }
            }
        }
    }

    private void checkDataLabelPropagation(DFDFlowGraphCollection flowGraph, String vertexName, List<String> expectedPropagatedLabels) {
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var sink = transposeFlowGraph.getSink();
            if (((DFDVertex) sink).getName()
                    .equals(vertexName)) {
                var propagatedLabels = retrieveDataLabels(sink);
                assertIterableEquals(expectedPropagatedLabels, propagatedLabels);
                return;
            }
        }
    }

    private void checkRealisticConstraints(DFDConfidentialityAnalysis analysis, DFDFlowGraphCollection flowGraph, int expectedViolationsFirstContraint,
            int expectedViolationsSecondContraint) {
        // Constraint 1: Personal data flowing to a node that is deployed outside the EU
        // Should find 1 violation
        int violationsFound = 0;
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal");
            });

            violationsFound += violations.size();
        }
        assertEquals(expectedViolationsFirstContraint, violationsFound);

        // Constraint 2: Personal data in a node deployed outside the EU w/o encryption
        // Should find 0 violations
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal") && !dataLabels.contains("Encrypted");
            });

            assertEquals(expectedViolationsSecondContraint, violations.size());
        }
    }

    private void checkIsNotCyclic(DFDFlowGraphCollection flowGraph) {
        assertFalse(flowGraph.wasCyclic());
    }

    private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
        return vertex.getAllVertexCharacteristics()
                .stream()
                .map(DFDCharacteristicValue.class::cast)
                .map(DFDCharacteristicValue::getValueName)
                .toList();
    }

    private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(DFDCharacteristicValue.class::cast)
                .map(DFDCharacteristicValue::getValueName)
                .toList();
    }
}
