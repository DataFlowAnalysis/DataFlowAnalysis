package org.palladiosimulator.dataflow.confidentiality.analysis;

import org.junit.jupiter.api.Test;

public class DataFlowConfidentialityAnalysisTest {

    @Test
    public void testBranchingOnlineShop() {      
        final var usageModelPath = "models/BranchingOnlineShop/default.usagemodel";
        final var allocationPath = "models/BranchingOnlineShop/default.allocation";

        final var analysis = new StandaloneDataFlowConfidentialtyAnalysis(usageModelPath, allocationPath);
        analysis.findAllSequences();
    }

}
