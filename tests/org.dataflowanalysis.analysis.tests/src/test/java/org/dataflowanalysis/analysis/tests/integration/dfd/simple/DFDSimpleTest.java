package org.dataflowanalysis.analysis.tests.integration.dfd.simple;

import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class DFDSimpleTest {
    private final Logger logger = LoggerManager.getLogger(DFDSimpleTest.class);

    private DFDConfidentialityAnalysis analysis;

    @Test
    public void testForUnusedInPinDetection() {
        String minimalDataFlowDiagramPath = Paths.get("models", "dfd", "UnusedInput", "default.dataflowdiagram")
                .toString();
        String minimalDataDictionaryPath = Paths.get("models", "dfd", "UnusedInput", "default.datadictionary")
                .toString();
        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                .build();

        this.analysis.initializeAnalysis();
        var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
        logger.info(exception.getMessage());
        assertEquals("DFD not simple: outPin not requiring all InPins", exception.getMessage());
    }

    @Test
    public void testForBranchingFlows() {
        String minimalDataFlowDiagramPath = Paths.get("models", "dfd", "Branching", "default.dataflowdiagram")
                .toString();
        String minimalDataDictionaryPath = Paths.get("models", "dfd", "Branching", "default.datadictionary")
                .toString();
        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                .build();

        this.analysis.initializeAnalysis();
        var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
        logger.info(exception.getMessage());
        assertEquals("DFD not simple: Number of flows to inpin not 1", exception.getMessage());
    }

    @Test
    public void testForDeadOutPin() {
        String minimalDataFlowDiagramPath = Paths.get("models", "dfd", "DeadOutPin", "default.dataflowdiagram")
                .toString();
        String minimalDataDictionaryPath = Paths.get("models", "dfd", "DeadOutPin", "default.datadictionary")
                .toString();
        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                .build();

        this.analysis.initializeAnalysis();
        var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
        logger.info(exception.getMessage());
        assertEquals("DFD not simple: Dead output pin", exception.getMessage());
    }

    @Test
    public void testForWrongFlowNames() {
        String minimalDataFlowDiagramPath = Paths.get("models", "dfd", "WrongFlowName", "default.dataflowdiagram")
                .toString();
        String minimalDataDictionaryPath = Paths.get("models", "dfd", "WrongFlowName", "default.datadictionary")
                .toString();
        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .useTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class)
                .build();

        this.analysis.initializeAnalysis();
        var exception = assertThrows(IllegalArgumentException.class, () -> analysis.findFlowGraphs());
        logger.info(exception.getMessage());
        assertEquals("DFD not simple: All outgoing flows from one pin must have the same name", exception.getMessage());
    }

}
