package org.dataflowanalysis.analysis.tests;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.AnalysisQuery;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.query.QueryDSL;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.analysis.tests.constraint.ConstraintTest;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * This class serves as self-contained analysis demonstration using the simple PCM and DFD instances of the online shop.
 * The two initialize methods showcase the analysis setup for PCM and DFD, while the test methods show how to analyze
 * both PCM and DFD models with and without using the ConstraintDSL, and also how to use the QueryDSL. The high degree
 * of code duplication within the tests (e.g., in manual querying) is intended for the sake of self-contained tests.
 */
@TestInstance(Lifecycle.PER_CLASS)
public class DemoTest {
    protected PCMDataFlowConfidentialityAnalysis pcmAnalysis;
    protected DFDConfidentialityAnalysis dfdAnalysis;

    private final Logger logger = Logger.getLogger(ConstraintTest.class);

    @BeforeAll
    public void initializePCMAnalysis() {
        final Path usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel");
        final Path allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation");
        final Path nodeCharacteristicsPath = Paths.get("models", "InternationalOnlineShop", "default.nodecharacteristics");

        pcmAnalysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath.toString())
                .useAllocationModel(allocationPath.toString())
                .useNodeCharacteristicsModel(nodeCharacteristicsPath.toString())
                .build();
        pcmAnalysis.initializeAnalysis();
    }

    @BeforeAll
    public void initializeDFDAnalysis() {
        final Path dataFlowDiagramPath = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram");
        final Path dataDictionaryPath = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary");

        dfdAnalysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(dataFlowDiagramPath.toString())
                .useDataDictionary(dataDictionaryPath.toString())
                .build();
        dfdAnalysis.initializeAnalysis();
    }

    @Test
    public void testPCMAnalysisUsingManualQuerying() {
        PCMFlowGraphCollection flowGraphs = pcmAnalysis.findFlowGraphs();
        flowGraphs.evaluate();

        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            List<? extends AbstractVertex<?>> violations = pcmAnalysis.queryDataFlow(transposeFlowGraph, node -> {

                List<String> serverLocation = node.getVertexCharacteristics("ServerLocation")
                        .stream()
                        .map(CharacteristicValue::getValueName)
                        .toList();
                List<String> dataSensitivity = node.getDataCharacteristicMap("DataSensitivity")
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .map(CharacteristicValue::getValueName)
                        .toList();

                return dataSensitivity.stream()
                        .anyMatch(l -> l.equals("Personal"))
                        && serverLocation.stream()
                                .anyMatch(l -> l.equals("nonEU"));
            });

            if (violations.size() > 0) {
                logger.info("Confidentiality violations found: %s".formatted(violations.toString()));
            } else {
                logger.info("No confidentiality violations found.");
            }
        }
    }

    @Test
    public void testPCMAnalysisUsingTheDSL() {
        PCMFlowGraphCollection flowGraphs = pcmAnalysis.findFlowGraphs();
        flowGraphs.evaluate();

        AnalysisConstraint constraint = new ConstraintDSL().ofData()
                .withLabel("DataSensitivity", List.of("Personal"))
                .fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("ServerLocation", "nonEU")
                .create();

        logger.info("Evaluating DSL constraint: \"%s\"".formatted(constraint.toString()));

        List<DSLResult> result = constraint.findViolations(flowGraphs);

        if (result.size() > 0) {
            logger.info("Confidentiality violations found: %s".formatted(result.toString()));
        } else {
            logger.info("No confidentiality violations found.");
        }
    }

    @Test
    public void testDFDAnalysisUsingManualQuerying() {
        DFDFlowGraphCollection flowGraphs = dfdAnalysis.findFlowGraphs();
        flowGraphs.evaluate();

        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
            List<? extends AbstractVertex<?>> violations = dfdAnalysis.queryDataFlow(transposeFlowGraph, node -> {

                List<String> serverLocation = node.getVertexCharacteristics("Location")
                        .stream()
                        .map(CharacteristicValue::getValueName)
                        .toList();
                List<String> dataSensitivity = node.getDataCharacteristicMap("Sensitivity")
                        .values()
                        .stream()
                        .flatMap(Collection::stream)
                        .map(CharacteristicValue::getValueName)
                        .toList();

                return dataSensitivity.stream()
                        .anyMatch(l -> l.equals("Personal"))
                        && serverLocation.stream()
                                .anyMatch(l -> l.equals("nonEU"));
            });

            if (violations.size() > 0) {
                logger.info("Confidentiality violations found: %s".formatted(violations.toString()));
            } else {
                logger.info("No confidentiality violations found.");
            }
        }

    }

    @Test
    public void testDFDAnalysisUsingTheDSL() {
        DFDFlowGraphCollection flowGraphs = dfdAnalysis.findFlowGraphs();
        flowGraphs.evaluate();

        AnalysisConstraint constraint = new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "nonEU")
                .create();

        logger.info("Evaluating DSL constraint: \"%s\"".formatted(constraint.toString()));

        List<DSLResult> result = constraint.findViolations(flowGraphs);

        if (result.size() > 0) {
            logger.info("Confidentiality violations found: %s".formatted(result.toString()));
        } else {
            logger.info("No confidentiality violations found.");
        }
    }

    @Test
    public void testQueryDSL() {
        PCMFlowGraphCollection pcmFlowGraphs = pcmAnalysis.findFlowGraphs();
        pcmFlowGraphs.evaluate();

        DFDFlowGraphCollection dfdFlowGraphs = dfdAnalysis.findFlowGraphs();
        dfdFlowGraphs.evaluate();

        AnalysisQuery pcmQuery = new QueryDSL().ofNode()
                .withCharacteristic("ServerLocation", "nonEU")
                .build();

        AnalysisQuery dfdQuery = new QueryDSL().ofNode()
                .withCharacteristic("Location", "nonEU")
                .build();

        var pcmResult = pcmQuery.query(pcmFlowGraphs)
                .stream()
                .map(DSLResult::getMatchedVertices)
                .flatMap(List::stream)
                .toList();

        var dfdResult = dfdQuery.query(dfdFlowGraphs)
                .stream()
                .map(DSLResult::getMatchedVertices)
                .flatMap(List::stream)
                .toList();

        logger.info("Non-EU Vertices found: %s and %s".formatted(pcmResult.toString(), dfdResult.toString()));
    }

}
