package org.dataflowanalysis.analysis.tests;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@Disabled("Until model update")
@TestInstance(Lifecycle.PER_CLASS)
public class BaseTest {
    protected DataFlowConfidentialityAnalysis onlineShopAnalysis;
    protected DataFlowConfidentialityAnalysis internationalOnlineShopAnalysis;
    protected DataFlowConfidentialityAnalysis travelPlannerAnalysis;

    @BeforeAll
    public void initializeOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation")
            .toString();

        onlineShopAnalysis = new DataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
        		.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
        		.legacy()
        		.usePluginActivator(Activator.class)
        		.useUsageModel(usageModelPath)
        		.useAllocationModel(allocationPath)
        		.build();

        onlineShopAnalysis.initializeAnalysis();
    }

    @BeforeAll
    public void initializeInternationalOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation")
            .toString();
        internationalOnlineShopAnalysis = new DataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
        		.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
        		.usePluginActivator(Activator.class)
        		.legacy()
        		.useUsageModel(usageModelPath)
        		.useAllocationModel(allocationPath)
        		.build();

        internationalOnlineShopAnalysis.initializeAnalysis();
    }

    @BeforeAll
    public void initializeTravelPlannerAnalysis() {
        final var usageModelPath = Paths.get("models", "TravelPlanner", "travelPlanner.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "TravelPlanner", "travelPlanner.allocation")
            .toString();

        travelPlannerAnalysis = new DataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
        		.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
        		.legacy()
        		.usePluginActivator(Activator.class)
        		.useUsageModel(usageModelPath)
        		.useAllocationModel(allocationPath)
        		.build();
        travelPlannerAnalysis.initializeAnalysis();
    }
    
    protected DataFlowConfidentialityAnalysis initializeAnalysis(Path usagePath, Path allocationPath) {
    	DataFlowConfidentialityAnalysis analysis = new DataFlowAnalysisBuilder()
        		.standalone()
        		.modelProjectName(TEST_MODEL_PROJECT_NAME)
        		.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
        		.legacy()
        		.usePluginActivator(Activator.class)
    			.useUsageModel(usagePath.toString())
    			.useAllocationModel(allocationPath.toString())
    			.build();
    	analysis.initializeAnalysis();
    	return analysis;
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
