package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.junit.jupiter.api.Test;

public class ConfidentialityAnalysisTest extends TestBase {

    @Override
    protected List<String> getModelsPath() {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    @Override
    protected void assignValues(List<Resource> list) {
        // TODO Auto-generated method stub
    }

    @Test
    public void testBranchingOnlineShop() {
        System.out.println("This is just for testing!");

        final var allocationURI = TestInitializer.getModelURI("models/BranchingOnlineShop/default.allocation");
        final var usageURI = TestInitializer.getModelURI("models/BranchingOnlineShop/default.usagemodel");

        final var analysis = new StandaloneDataFlowConfidentialtyAnalysis(usageURI, allocationURI);
        analysis.findAllSequences();
    }

}
