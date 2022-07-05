package org.palladiosimulator.dataflow.confidentiality.analysis;

import org.junit.jupiter.api.Test;

public class DataFlowConfidentialityAnalysisTest {

    @Test
    public void testStandaloneAnalysis() {      
        final var usageModelPath = "models/BranchingOnlineShop/default.usagemodel";
        final var allocationPath = "models/BranchingOnlineShop/default.allocation";

        final DataFlowConfidentialityAnalysis analysis = new StandaloneDataFlowConfidentialtyAnalysis(usageModelPath, allocationPath);
        
        analysis.initalizeAnalysis();
        analysis.loadModels();
        analysis.findAllSequences();
    }

}
