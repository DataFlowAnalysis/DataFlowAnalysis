package org.dataflowanalysis.analysis.tests;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class BaseTest {
    protected DataFlowConfidentialityAnalysis onlineShopAnalysis;
    protected DataFlowConfidentialityAnalysis internationalOnlineShopAnalysis;
    protected DataFlowConfidentialityAnalysis travelPlannerAnalysis;

    @BeforeAll
    public void initializeOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel");
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation");
        final var nodeCharacteristicsPath = Paths.get("models", "BranchingOnlineShop", "default.nodecharacteristics");

        onlineShopAnalysis = this.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
        onlineShopAnalysis.initializeAnalysis();
    }

    @BeforeAll
    public void initializeInternationalOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel");
        final var allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation");
        final var nodeCharacteristicsPath = Paths.get("models", "InternationalOnlineShop", "default.nodecharacteristics");
        
        internationalOnlineShopAnalysis = this.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
        internationalOnlineShopAnalysis.initializeAnalysis();
    }

    @BeforeAll
    public void initializeTravelPlannerAnalysis() {
        final var usageModelPath = Paths.get("models", "TravelPlanner", "travelPlanner.usagemodel");
        final var allocationPath = Paths.get("models", "TravelPlanner", "travelPlanner.allocation");
        final var nodeCharacteristicsPath = Paths.get("models", "TravelPlanner", "travelPlanner.nodecharacteristics");

        travelPlannerAnalysis = this.initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
        travelPlannerAnalysis.initializeAnalysis();
    }
    
    protected DataFlowConfidentialityAnalysis initializeAnalysis(Path usagePath, Path allocationPath, Path nodePath) {
    	DataFlowConfidentialityAnalysis analysis = new DataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
        		.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
    			.usePluginActivator(Activator.class)
    			.useUsageModel(usagePath.toString())
    			.useAllocationModel(allocationPath.toString())
    			.useNodeCharacteristicsModel(nodePath.toString())
    			.build();
    	analysis.initializeAnalysis();
    	return analysis;
    }
}
