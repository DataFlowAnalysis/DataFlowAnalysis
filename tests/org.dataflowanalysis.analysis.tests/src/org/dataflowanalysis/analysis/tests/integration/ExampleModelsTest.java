package org.dataflowanalysis.analysis.tests.integration;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.examplemodels.results.ExampleModelResult;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ExampleModelsTest {
    private final Logger logger = Logger.getLogger(ExampleModelsTest.class);

    private static Stream<Arguments> provideExampleModelViolations() {
        return Stream.of(
                Arguments.of(new InternationalOnlineShopResult()),
                Arguments.of(new BranchingOnlineShopResult()),
                Arguments.of(new TravelPlannerResult()),
                Arguments.of(new MultipleDeploymentsResult()),
                Arguments.of(new VariableReturnResult())
        );
    }

    @ParameterizedTest
    @MethodSource("provideExampleModelViolations")
    public void shouldReturnCorrectViolations(ExampleModelResult exampleModelResult) {
        logger.info(String.format("Testing model: %s", exampleModelResult.getModelName()));
        if (exampleModelResult instanceof PCMExampleModelResult pcmExampleModelResult) {
            this.shouldReturnCorrectViolationsPCMModel(pcmExampleModelResult);
        }
    }

    private void shouldReturnCorrectViolationsPCMModel(PCMExampleModelResult exampleModelResult) {
        PCMDataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
                .standalone()
                .modelProjectName(exampleModelResult.getModelProjectName())
                .usePluginActivator(exampleModelResult.getPluginActivator())
                .useUsageModel(exampleModelResult.getUsageModelPath())
                .useAllocationModel(exampleModelResult.getAllocationModelPath())
                .useNodeCharacteristicsModel(exampleModelResult.getNodeCharacteristicsModelPath())
                .build();
        analysis.initializeAnalysis();
        PCMFlowGraphCollection flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();
        List<DSLResult> violatingVertices = exampleModelResult.getDSLConstraint().findViolations(flowGraphs);
        if (exampleModelResult.getExpectedViolations().isEmpty()) {
            assertTrue(violatingVertices.isEmpty(), "Analysis found violating vertices, but none were expected");
        }
        for (ExpectedViolation expectedViolation : exampleModelResult.getExpectedViolations()) {
            Optional<? extends AbstractVertex<?>> violatingVertex = violatingVertices.stream()
                    .map(DSLResult::getMatchedVertices)
                    .flatMap(List::stream)
                    .filter(expectedViolation::references)
                    .findAny();
            if (violatingVertex.isEmpty()) {
                logger.error(String.format("Could not find vertex with id: %s", expectedViolation.getNodeID()));
                    fail(String.format("Could not find vertex with id: %s", expectedViolation.getNodeID()));
            }

            List<ExpectedCharacteristic> missingNodeCharacteristics = expectedViolation.hasNodeCharacteristic(violatingVertex.get().getAllVertexCharacteristics());
            if (!missingNodeCharacteristics.isEmpty()) {
                logger.error(String.format("Vertex %s is missing the following node characteristics: %s", violatingVertex.get(), missingNodeCharacteristics));
                fail(String.format("Vertex %s is missing the following node characteristics: %s", violatingVertex.get(), missingNodeCharacteristics));
            }

            Map<String, List<ExpectedCharacteristic>> missingDataCharacteristics = expectedViolation.hasDataCharacteristics(violatingVertex.get().getAllDataCharacteristics());
            if (!missingDataCharacteristics.isEmpty()) {
                logger.error(String.format("Vertex %s is missing the following node characteristics: %s", violatingVertex.get(), missingDataCharacteristics));
                fail(String.format("Vertex %s is missing the following node characteristics: %s", violatingVertex.get(), missingDataCharacteristics));
            }
        }
    }
}
