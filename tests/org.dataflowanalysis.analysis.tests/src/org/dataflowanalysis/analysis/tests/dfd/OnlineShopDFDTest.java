package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.file.Paths;
import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnlineShopDFDTest {

    private DFDConfidentialityAnalysis analysis;

    @BeforeEach
    public void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram");
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary");

        this.analysis = new DFDDataFlowAnalysisBuilder().standalone().modelProjectName(TEST_MODEL_PROJECT_NAME).usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString()).useDataDictionary(dataDictionaryPath.toString()).build();

        this.analysis.initializeAnalysis();
    }

    @Test
    public void numberOfPartialFlowGraphs_equalsThree() {
        DFDFlowGraph flowGraph = analysis.findFlowGraph();
        assertEquals(flowGraph.getPartialFlowGraphs().size(), 3);
    }

    @Test
    public void checkSinks() {
        var flowGraph = analysis.findFlowGraph();
        var entityNames = flowGraph.getPartialFlowGraphs().stream().map(pfg -> ((DFDVertex) pfg.getSink()).getName()).toList();

        var expectedNames = List.of("User", "Database", "Database");
        assertIterableEquals(expectedNames, entityNames);
    }

    @Test
    public void testNodeLabels() {
        var flowGraph = analysis.findFlowGraph();
        var propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

        for (var partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            for (var vertex : partialFlowGraph.getVertices()) {
                if (((DFDVertex) vertex).getName().equals("User")) {
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
        var flowGraph = analysis.findFlowGraph();
        var propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);
        for (var partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            var sink = partialFlowGraph.getSink();
            if (((DFDVertex) sink).getName().equals("User")) {
                var propagatedLabels = retrieveDataLabels(sink);
                var expectedPropagatedLables = List.of("Public");
                assertIterableEquals(expectedPropagatedLables, propagatedLabels);
                return;
            }
        }
    }

    @Test
    public void testRealisticConstraints() {
        var flowGraph = analysis.findFlowGraph();
        var propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

        // Constraint 1: Personal data flowing to a node that is deployed outside the EU
        // Should find 1 violation
        int violationsFound = 0;
        for (var partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            var violations = analysis.queryDataFlow(partialFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal");
            });

            violationsFound += violations.size();
        }
        assertEquals(1, violationsFound);

        // Constraint 2: Personal data in a node deployed outside the EU w/o encryption
        // Should find 0 violations
        for (var partialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            var violations = analysis.queryDataFlow(partialFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal") && !dataLabels.contains("Encrypted");
            });

            assertEquals(0, violations.size());
        }
    }

    private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
        return vertex.getAllNodeCharacteristics().stream().map(DFDCharacteristicValue.class::cast).map(DFDCharacteristicValue::getValueName).toList();
    }

    private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
        return vertex.getAllDataFlowVariables().stream().map(DataFlowVariable::getAllCharacteristics).flatMap(List::stream)
                .map(DFDCharacteristicValue.class::cast).map(DFDCharacteristicValue::getValueName).toList();
    }
}
