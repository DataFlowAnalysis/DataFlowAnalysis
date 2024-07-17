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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class OnlineShopTestDFD {

    private static DFDConfidentialityAnalysis cylcicAnalysis;
    private static DFDConfidentialityAnalysis linearAnalysis;

    @BeforeAll
    public static void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();

        cylcicAnalysis = new DFDDataFlowAnalysisBuilder().standalone()
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

        cylcicAnalysis.initializeAnalysis();
        linearAnalysis.initializeAnalysis();
    }

    @Test
    public void testCyclicAnalysis() {
        runAllChecksForAnalysis(cylcicAnalysis);
    }

    @Test
    public void testLinearAnalysis() {
        runAllChecksForAnalysis(linearAnalysis);
    }

    private void runAllChecksForAnalysis(DFDConfidentialityAnalysis analysis) {
        checkNumberOfTransposeFlowGraphs(analysis, 3);
        checkSinks(analysis, List.of("User", "Database", "Database"));
        checkNodeLabels(analysis, "User", List.of("EU"));
        checkDataLabelPropagation(analysis, "User", List.of("Public"));
        checkRealisticConstraints(analysis, 1, 0);
        checkIsNotCyclic(analysis);
    }

    private void checkNumberOfTransposeFlowGraphs(DFDConfidentialityAnalysis analysis, int expectedNumber) {
        DFDFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        assertEquals(flowGraph.getTransposeFlowGraphs()
                .size(), expectedNumber);
    }

    private void checkSinks(DFDConfidentialityAnalysis analysis, List<String> expectedNames) {
        var flowGraph = analysis.findFlowGraphs();
        var entityNames = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();

        assertIterableEquals(expectedNames, entityNames);
    }

    private void checkNodeLabels(DFDConfidentialityAnalysis analysis, String vertexName, List<String> expectedLabels) {
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

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

    private void checkDataLabelPropagation(DFDConfidentialityAnalysis analysis, String vertexName, List<String> expectedPropagatedLabels) {
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
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

    private void checkRealisticConstraints(DFDConfidentialityAnalysis analysis, int expectedViolationsFirstContraint,
            int expectedViolationsSecondContraint) {
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

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

    private void checkIsNotCyclic(DFDConfidentialityAnalysis analysis) {
        var flowGraph = analysis.findFlowGraphs();
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
