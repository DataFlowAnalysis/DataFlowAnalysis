package org.palladiosimulator.dataflow.confidentiality.analysis;

import org.junit.jupiter.api.Assertions;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
import org.junit.jupiter.api.Test;

public class DataFlowConfidentialityAnalysisTest {

    private static String TEST_MODEL_PROJECT_PATH = "org.palladiosimulator.dataflow.confidentiality.analysis.testmodels";

    @Test
    public void testStandaloneAnalysis() {
        final var usageModelPath = "models/BranchingOnlineShop/default.usagemodel";
        final var allocationPath = "models/BranchingOnlineShop/default.allocation";

        final DataFlowConfidentialityAnalysis analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(
                TEST_MODEL_PROJECT_PATH, Activator.class, usageModelPath, allocationPath);

        analysis.initalizeAnalysis();
        analysis.loadModels();
        Assertions.assertFalse(analysis.findAllSequences()
            .isEmpty());
    }

}
