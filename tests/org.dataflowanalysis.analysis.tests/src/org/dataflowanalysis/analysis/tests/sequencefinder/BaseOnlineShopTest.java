package org.dataflowanalysis.analysis.tests.sequencefinder;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.file.Paths;
import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseOnlineShopTest {

    private DFDConfidentialityAnalysis analysis;
    private DFDFlowGraphCollection flowGraph;

    @BeforeAll
    public void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram");
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary");

        analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString())
                .useDataDictionary(dataDictionaryPath.toString())
                .useTransposeFlowGraphFinder(getTransposeFlowGraphFinder())
                .build();

        analysis.initializeAnalysis();

        flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
    }

    protected abstract Class<? extends TransposeFlowGraphFinder> getTransposeFlowGraphFinder();

    @Test
    public void numberOfTransposeFlowGraphs_equalsThree() {
        DFDFlowGraphCollection flowGraph = analysis.findFlowGraphs();
        assertEquals(flowGraph.getTransposeFlowGraphs()
                .size(), 3);
    }

    @Test
    public void checkSinks() {
        var entityNames = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();

        var expectedNames = List.of("User", "Database", "Database");
        assertIterableEquals(expectedNames, entityNames);
    }

    @Test
    public void testNodeLabels() {
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : transposeFlowGraph.getVertices()) {
                if (((DFDVertex) vertex).getName()
                        .equals("User")) {
                    var userVertexLabels = retrieveNodeLabels(vertex);
                    var expectedLabels = List.of("EU");
                    assertIterableEquals(expectedLabels, userVertexLabels);
                    return;
                }
            }
        }
    }

    @Test
    public void testDataLabelPropagation() {
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var sink = transposeFlowGraph.getSink();
            if (((DFDVertex) sink).getName()
                    .equals("User")) {
                var propagatedLabels = retrieveDataLabels(sink);
                var expectedPropagatedLabels = List.of("Public");
                assertIterableEquals(expectedPropagatedLabels, propagatedLabels);
                return;
            }
        }
    }

    @Test
    public void testRealisticConstraints() {
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
        assertEquals(1, violationsFound);

        // Constraint 2: Personal data in a node deployed outside the EU w/o encryption
        // Should find 0 violations
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal") && !dataLabels.contains("Encrypted");
            });

            assertEquals(0, violations.size());
        }
    }

    @Test
    public void checkIsNotCyclic() {
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
