package org.palladiosimulator.dataflow.confidentiality.analysis;

import org.junit.jupiter.api.Test;

public class DataFlowConfidentialityAnalysisTest {
    
    private static String TEST_MODEL_PROJECT_PATH = "../../tests/org.palladiosimulator.dataflow.confidentiality.analysis.testmodels";
    
    private static String createModelPath(String relativePath) {
        return String.format("%s/%s", TEST_MODEL_PROJECT_PATH, relativePath);
    }

    @Test
    public void testStandaloneAnalysis() {      
        final var usageModelPath = createModelPath("models/BranchingOnlineShop/default.usagemodel");
        final var allocationPath = createModelPath("models/BranchingOnlineShop/default.allocation");

        final DataFlowConfidentialityAnalysis analysis = new StandaloneDataFlowConfidentialtyAnalysis(usageModelPath, allocationPath);
        
        analysis.initalizeAnalysis();
        analysis.loadModels();
        analysis.findAllSequences();
    }

}
