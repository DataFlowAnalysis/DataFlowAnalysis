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

public class OnlineShopTest {

    private DFDConfidentialityAnalysis cyclicAnalysis;
    private DFDConfidentialityAnalysis acyclicAnalysis;
    
    

    @BeforeEach
    public void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram").toString();
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary").toString();
        
        
        
        this.cyclicAnalysis = new DFDDataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDCyclicTransposeFlowGraphFinder.class)
                .build();
        
        this.acyclicAnalysis = new DFDDataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath)
                .useDataDictionary(dataDictionaryPath)
                .useTransposeFlowGraphFinder(DFDTransposeFlowGraphFinder.class)
                .build();

        this.cyclicAnalysis.initializeAnalysis();
        this.acyclicAnalysis.initializeAnalysis();
    }

    @Test
    public void numberOfTransposeFlowGraphs_equalsThree() {
        DFDFlowGraphCollection cyclicFlowGraph = cyclicAnalysis.findFlowGraphs();
        DFDFlowGraphCollection acyclicFlowGraph = acyclicAnalysis.findFlowGraphs();
        assertEquals(acyclicFlowGraph.getTransposeFlowGraphs().size(), 3);
        assertEquals(cyclicFlowGraph.getTransposeFlowGraphs().size(), acyclicFlowGraph.getTransposeFlowGraphs().size());
    }

    @Test
    public void checkSinks() {
        var flowGraph = acyclicAnalysis.findFlowGraphs();
        var cyclicFlowGraph = cyclicAnalysis.findFlowGraphs();
        
        var entityNames = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();
        
        
        var cyclicentityNames = cyclicFlowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();

        var expectedNames = List.of("User", "Database", "Database");
        assertIterableEquals(expectedNames, entityNames);
        assertIterableEquals(expectedNames, cyclicentityNames);
    }

    @Test
    public void testNodeLabels() {
        var flowGraph = acyclicAnalysis.findFlowGraphs();
        var cyclicFlowGraph = cyclicAnalysis.findFlowGraphs();
        
        flowGraph.evaluate();
        cyclicFlowGraph.evaluate();
        
        checkNodeLabel(flowGraph);
        checkNodeLabel(cyclicFlowGraph);
        
    }
    public void checkNodeLabel(DFDFlowGraphCollection flowGraph){
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : transposeFlowGraph.getVertices()) {
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
        var flowGraph = acyclicAnalysis.findFlowGraphs();
        var cyclicFlowGraph = cyclicAnalysis.findFlowGraphs();
        
        flowGraph.evaluate();
        cyclicFlowGraph.evaluate();
        
        DataLabelPropagation(flowGraph);
        DataLabelPropagation(cyclicFlowGraph);
    }
    
    public void DataLabelPropagation(DFDFlowGraphCollection flowGraph){
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var sink = transposeFlowGraph.getSink();
            if (((DFDVertex) sink).getName().equals("User")) {
                var propagatedLabels = retrieveDataLabels(sink);
                var expectedPropagatedLabels = List.of("Public");
                assertIterableEquals(expectedPropagatedLabels, propagatedLabels);
                return;
            }
        }
    }
    
    
    //cyclic tested by microsecnd test
    @Test
    public void testRealisticConstraints() {
        var flowGraph = acyclicAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        // Constraint 1: Personal data flowing to a node that is deployed outside the EU
        // Should find 1 violation
        int violationsFound = 0;
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = acyclicAnalysis.queryDataFlow(transposeFlowGraph, it -> {
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
            var violations = acyclicAnalysis.queryDataFlow(transposeFlowGraph, it -> {
                var nodeLabels = retrieveNodeLabels(it);
                var dataLabels = retrieveDataLabels(it);

                return nodeLabels.contains("nonEU") && dataLabels.contains("Personal")
                        && !dataLabels.contains("Encrypted");
            });

            assertEquals(0, violations.size());
        }
    }

    @Test
    public void testIsNotCyclic() {
        var flowGraph = cyclicAnalysis.findFlowGraphs();
        assertFalse(flowGraph.wasCyclic());
    }

    private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
        return vertex.getAllVertexCharacteristics().stream().map(DFDCharacteristicValue.class::cast)
                .map(DFDCharacteristicValue::getValueName).toList();
    }

    private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics().stream().map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream).map(DFDCharacteristicValue.class::cast).map(DFDCharacteristicValue::getValueName)
                .toList();
    }
}
