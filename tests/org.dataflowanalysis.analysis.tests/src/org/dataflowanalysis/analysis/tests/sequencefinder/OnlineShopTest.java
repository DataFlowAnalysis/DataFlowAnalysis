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
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

public class OnlineShopTest {

    private DFDConfidentialityAnalysis cyclicAnalysis;
    private DFDConfidentialityAnalysis acyclicAnalysis;
    
    
    private DFDCyclicTransposeFlowGraphFinder dfdCyclicTransposeFlowGraphFinder;
    private DFDTransposeFlowGraphFinder dfdTransposeFlowGraphFinder;

    @BeforeEach
    public void initAnalysis() {
        final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram").toString();
        final var dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary").toString();
        
        var resourceProvider = new DFDURIResourceProvider(ResourceUtils.createRelativePluginURI(dataFlowDiagramPath, TEST_MODEL_PROJECT_NAME),
                ResourceUtils.createRelativePluginURI(dataDictionaryPath, TEST_MODEL_PROJECT_NAME));
        
        //Class<? extends Plugin> modelProjectActivator = Activator.class;
        
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
        .put("dataflowdiagram", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
        .put("datadictionary", new XMIResourceFactoryImpl());

        EcorePlugin.ExtensionProcessor.process(null);

        try {
            var initializationBuilder = StandaloneInitializerBuilder.builder()
                    .registerProjectURI(DFDConfidentialityAnalysis.class, DFDConfidentialityAnalysis.PLUGIN_PATH);
        
            //modelProjectActivator.ifPresent(projectActivator -> initializationBuilder.registerProjectURI(projectActivator, this.modelProjectName));
        
            initializationBuilder.build()
                    .init();
        
        
        } catch (StandaloneInitializationException e) {
            throw new IllegalStateException("Could not initialize analysis");
        }
        resourceProvider.loadRequiredResources();
        if (resourceProvider.sufficientResourcesLoaded()) {
            throw new IllegalStateException("Could not initialize analysis");
        }
        
        dfdCyclicTransposeFlowGraphFinder = new DFDCyclicTransposeFlowGraphFinder(resourceProvider);
        dfdTransposeFlowGraphFinder = new DFDTransposeFlowGraphFinder(resourceProvider);
        
        
        this.cyclicAnalysis = new DFDDataFlowAnalysisBuilder().standalone().modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class).useCustomResourceProvider(resourceProvider)
                .useTransposeFlowGraphFinder(dfdCyclicTransposeFlowGraphFinder).build();
        
        this.acyclicAnalysis = new DFDDataFlowAnalysisBuilder().standalone().modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class).useCustomResourceProvider(resourceProvider)
                .useTransposeFlowGraphFinder(dfdTransposeFlowGraphFinder).build();

        //this.analysis.initializeAnalysis();
    }

    @Test
    public void numberOfTransposeFlowGraphs_equalsThree() {
        DFDFlowGraphCollection flowGraph = cyclicAnalysis.findFlowGraphs();
        assertEquals(flowGraph.getTransposeFlowGraphs().size(), 3);
    }

    @Test
    public void checkSinks() {
        var flowGraph = cyclicAnalysis.findFlowGraphs();
        var entityNames = flowGraph.getTransposeFlowGraphs()
                .stream()
                .map(it -> ((DFDVertex) it.getSink()).getName())
                .toList();

        var expectedNames = List.of("User", "Database", "Database");
        assertIterableEquals(expectedNames, entityNames);
    }

    @Test
    public void testNodeLabels() {
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

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
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();
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

    @Test
    public void testRealisticConstraints() {
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
        assertEquals(1, violationsFound);

        // Constraint 2: Personal data in a node deployed outside the EU w/o encryption
        // Should find 0 violations
        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
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
        var flowGraph = analysis.findFlowGraphs();
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
