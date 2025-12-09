package org.dataflowanalysis.analysis.tests.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.examplemodels.results.ExampleModelResult;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.models.BranchingResult;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.AppleWallet;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.CMACaseStudy;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.CWANoViolation;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.CWAPersonalDataViolation;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.CWARPIViolation;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.ComponentTestingCaseStudy;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.DocProc;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.ECUUpdateCaseStudy;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.HipaaCaseStudy;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.MiniTwitCaseStudy;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.OnlineShopResult;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.SimpleOnlineShopResult;
import org.dataflowanalysis.examplemodels.results.dfd.scenarios.VWCariad;
import org.dataflowanalysis.examplemodels.results.pcm.*;
import org.dataflowanalysis.examplemodels.results.pcm.models.CompositeResult;
import org.dataflowanalysis.examplemodels.results.pcm.models.IgnoredNodesResult;
import org.dataflowanalysis.examplemodels.results.pcm.models.MultipleDeploymentsResult;
import org.dataflowanalysis.examplemodels.results.pcm.models.NodeCharacteristicsResult;
import org.dataflowanalysis.examplemodels.results.pcm.models.VariableReturnResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.BankBranchesResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.BranchingOnlineShopResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.CoronaWarnAppResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.EVerest;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.InternationalOnlineShopResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.MaaSTicketSystemResult;
import org.dataflowanalysis.examplemodels.results.pcm.scenarios.TravelPlannerResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ExampleModelsTest {
    private final Logger logger = LoggerManager.getLogger(ExampleModelsTest.class);

    private static Stream<Arguments> providePCMExampleModelViolations() {
        return Stream.of(Arguments.of(new BankBranchesResult()), Arguments.of(new BranchingOnlineShopResult()), Arguments.of(new CompositeResult()),
                Arguments.of(new CoronaWarnAppResult()), Arguments.of(new IgnoredNodesResult()), Arguments.of(new InternationalOnlineShopResult()),
                Arguments.of(new MaaSTicketSystemResult()), Arguments.of(new MultipleDeploymentsResult()),
                Arguments.of(new NodeCharacteristicsResult()), Arguments.of(new TravelPlannerResult()), Arguments.of(new VariableReturnResult()),
                Arguments.of(new EVerest()));
    }

    private static Stream<Arguments> provideDFDExampleModelViolations() {
        return Stream.of(Arguments.of(new BranchingResult()), Arguments.of(new OnlineShopResult()), Arguments.of(new SimpleOnlineShopResult()),
                Arguments.of(new CWANoViolation()), Arguments.of(new VWCariad()), Arguments.of(new CWAPersonalDataViolation()),
                Arguments.of(new CWARPIViolation()), Arguments.of(new ComponentTestingCaseStudy()), Arguments.of(new ECUUpdateCaseStudy()),
                Arguments.of(new MiniTwitCaseStudy()), Arguments.of(new HipaaCaseStudy()), Arguments.of(new DocProc()),
                Arguments.of(new AppleWallet()), Arguments.of(new CMACaseStudy()));
    }

    @ParameterizedTest
    @MethodSource("providePCMExampleModelViolations")
    public void shouldReturnCorrectViolationsPCM(ExampleModelResult exampleModelResult) {
        logger.info(String.format("Testing model: %s", exampleModelResult.getModelName()));
        if (exampleModelResult instanceof PCMExampleModelResult pcmExampleModelResult) {
            this.shouldReturnCorrectViolationsPCMModel(pcmExampleModelResult);
        }
    }

    private void shouldReturnCorrectViolationsPCMModel(PCMExampleModelResult exampleModelResult) {
        PCMDataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(exampleModelResult.getModelProjectName())
                .usePluginActivator(exampleModelResult.getPluginActivator())
                .useUsageModel(exampleModelResult.getUsageModelPath())
                .useAllocationModel(exampleModelResult.getAllocationModelPath())
                .useNodeCharacteristicsModel(exampleModelResult.getNodeCharacteristicsModelPath())
                .build();
        shouldReturnCorrectViolations(analysis, exampleModelResult);
    }

    @ParameterizedTest
    @MethodSource("provideDFDExampleModelViolations")
    public void shouldReturnCorrectViolationsDFD(ExampleModelResult exampleModelResult) {
        logger.info(String.format("Testing model: %s", exampleModelResult.getModelName()));
        if (exampleModelResult instanceof DFDExampleModelResult dfdExampleModelResult) {
            this.shouldReturnCorrectViolationsDFDModel(dfdExampleModelResult);
        }
    }

    private void shouldReturnCorrectViolationsDFDModel(DFDExampleModelResult exampleModelResult) {
        DFDConfidentialityAnalysis analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(exampleModelResult.getModelProjectName())
                .usePluginActivator(exampleModelResult.getPluginActivator())
                .useDataFlowDiagram(exampleModelResult.getDataFlowDiagram())
                .useDataDictionary(exampleModelResult.getDataDictionary())
                .build();
        shouldReturnCorrectViolations(analysis, exampleModelResult);
    }

    private void shouldReturnCorrectViolations(DataFlowConfidentialityAnalysis analysis, ExampleModelResult exampleModelResult) {
        Assumptions.assumeTrue(!exampleModelResult.getDSLConstraints()
                .isEmpty(), "Example Model does not define any constraints!");
        analysis.initializeAnalysis();
        FlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();
        List<DSLResult> violatingVertices = exampleModelResult.getDSLConstraints()
                .stream()
                .map(constraint -> constraint.findViolations(flowGraphs))
                .flatMap(List::stream)
                .toList();
        if (exampleModelResult.getExpectedViolations()
                .isEmpty() && !violatingVertices.isEmpty()) {
            logger.error("Offending violations:" + violatingVertices);
            fail("Analysis found violating vertices, but none were expected");
        }
        for (ExpectedViolation expectedViolation : exampleModelResult.getExpectedViolations()) {
            Optional<? extends AbstractVertex<?>> violatingVertex = Optional.empty();
            for (DSLResult violation : violatingVertices) {
                if (violatingVertex.isPresent())
                    continue;
                int flowGraphIndex = flowGraphs.getTransposeFlowGraphs()
                        .indexOf(violation.getTransposeFlowGraph());
                assertTrue(flowGraphIndex != -1);
                violatingVertex = violation.getMatchedVertices()
                        .stream()
                        .filter(it -> expectedViolation.references(it, flowGraphIndex))
                        .findAny();
            }
            if (violatingVertex.isEmpty()) {
                logger.error(String.format("Could not find vertex with id %s in TFG with index %s", expectedViolation.getIdentifier(),
                        expectedViolation.getFlowGraphIndex()));
                logger.error("Found the following violations:");
                for (DSLResult violatingTFG : violatingVertices) {
                    logger.error(String.format("Index %s: %s", flowGraphs.getTransposeFlowGraphs()
                            .indexOf(violatingTFG.getTransposeFlowGraph()), violatingTFG));
                }
                fail(String.format("Could not find vertex with id %s in TFG with index %s", expectedViolation.getIdentifier(),
                        expectedViolation.getFlowGraphIndex()));
            }
            testViolations(violatingVertex.get(), expectedViolation, expectedViolation.getFlowGraphIndex());
        }
        for (DSLResult violatingFlowGraph : violatingVertices) {
            int transposeFlowGraphIndex = flowGraphs.getTransposeFlowGraphs()
                    .indexOf(violatingFlowGraph.getTransposeFlowGraph());
            for (var violatingVertex : violatingFlowGraph.getMatchedVertices()) {
                Optional<ExpectedViolation> expectedViolation = exampleModelResult.getExpectedViolations()
                        .stream()
                        .filter(it -> it.references(violatingVertex, transposeFlowGraphIndex))
                        .findAny();
                if (expectedViolation.isEmpty()) {
                    logger.error(String.format("Could not find expected violation for vertex with id %s in TFG at index %s!",
                            violatingVertex.toString(), transposeFlowGraphIndex));
                    logger.error("Found the following violations:");
                    for (DSLResult violatingTFG : violatingVertices) {
                        logger.error(String.format("Index %s: %s", flowGraphs.getTransposeFlowGraphs()
                                .indexOf(violatingTFG.getTransposeFlowGraph()), violatingTFG));
                    }
                    logger.error(String.format("Expected the following violations: %s", exampleModelResult.getExpectedViolations()));
                    fail(String.format("Could not find expected violation for vertex with id %s in TFG at index %s!", violatingVertex.toString(),
                            transposeFlowGraphIndex));
                }
                testViolations(violatingVertex, expectedViolation.get(), transposeFlowGraphIndex);
            }
        }
    }

    private void testViolations(AbstractVertex<?> violatingVertex, ExpectedViolation expectedViolation, int transposeFlowGraphIndex) {
        List<ExpectedCharacteristic> missingNodeCharacteristics = expectedViolation
                .hasNodeCharacteristic(violatingVertex.getAllVertexCharacteristics());
        if (!missingNodeCharacteristics.isEmpty()) {
            logger.error(String.format("Vertex %s at TFG %s is missing the following node characteristics: %s", violatingVertex,
                    transposeFlowGraphIndex, missingNodeCharacteristics));
            fail(String.format("Vertex %s is missing the following node characteristics: %s", violatingVertex, missingNodeCharacteristics));
        }

        var incorrectNodeCharacteristics = expectedViolation.hasIncorrectNodeCharacteristics(violatingVertex.getAllVertexCharacteristics());
        if (!incorrectNodeCharacteristics.isEmpty()) {
            logger.error(String.format("Vertex %s at TFG %s has the following incorrect node characteristics: %s", violatingVertex,
                    transposeFlowGraphIndex, incorrectNodeCharacteristics));
            fail(String.format("Vertex %s has the following incorrect node characteristics: %s", violatingVertex, incorrectNodeCharacteristics));
        }

        Map<String, List<ExpectedCharacteristic>> missingDataCharacteristics = expectedViolation
                .hasDataCharacteristics(violatingVertex.getAllDataCharacteristics());
        if (!missingDataCharacteristics.isEmpty()) {
            logger.error(String.format("Vertex %s at TFG %s is missing the following data characteristics: %s", violatingVertex,
                    transposeFlowGraphIndex, missingDataCharacteristics));
            fail(String.format("Vertex %s is missing the following data characteristics: %s", violatingVertex, missingDataCharacteristics));
        }

        var incorrectDataCharacteristics = expectedViolation.hasMissingDataCharacteristics(violatingVertex.getAllDataCharacteristics());
        if (!incorrectDataCharacteristics.isEmpty()) {
            logger.error(String.format("Vertex %s at TFG %s has the following incorrect data characteristics: %s", violatingVertex,
                    transposeFlowGraphIndex, incorrectDataCharacteristics));
            fail(String.format("Vertex %s has the following incorrect data characteristics: %s", violatingVertex, incorrectDataCharacteristics));
        }
    }
}
