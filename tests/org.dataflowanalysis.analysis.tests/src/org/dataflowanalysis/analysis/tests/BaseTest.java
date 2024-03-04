package org.dataflowanalysis.analysis.tests;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class BaseTest {
    protected PCMDataFlowConfidentialityAnalysis onlineShopAnalysis;
    protected PCMDataFlowConfidentialityAnalysis internationalOnlineShopAnalysis;
    protected PCMDataFlowConfidentialityAnalysis travelPlannerAnalysis;

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
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
    }

    protected PCMDataFlowConfidentialityAnalysis initializeAnalysis(Path usagePath, Path allocationPath, Path nodePath) {
        PCMDataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME).usePluginActivator(Activator.class).useUsageModel(usagePath.toString())
                .useAllocationModel(allocationPath.toString()).useNodeCharacteristicsModel(nodePath.toString()).build();
        analysis.initializeAnalysis();
        return analysis;
    }
}
