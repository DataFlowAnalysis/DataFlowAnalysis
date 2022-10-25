package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

public class BaseTest {
    protected static StandalonePCMDataFlowConfidentialtyAnalysis onlineShopAnalysis;
    protected static StandalonePCMDataFlowConfidentialtyAnalysis internationalOnlineShopAnalysis;
    protected static StandalonePCMDataFlowConfidentialtyAnalysis travelPlannerAnalysis;

    @BeforeAll
    public static void initializeOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation")
            .toString();

        onlineShopAnalysis = new StandalonePCMDataFlowConfidentialtyAnalysis(TEST_MODEL_PROJECT_NAME, Activator.class,
                usageModelPath, allocationPath);

        onlineShopAnalysis.initalizeAnalysis();
    }

    @BeforeAll
    public static void initializeInternationalOnlineShopAnalysis() {
        final var usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation")
            .toString();

        internationalOnlineShopAnalysis = new StandalonePCMDataFlowConfidentialtyAnalysis(TEST_MODEL_PROJECT_NAME,
                Activator.class, usageModelPath, allocationPath);

        internationalOnlineShopAnalysis.initalizeAnalysis();
    }

    @BeforeAll
    public static void initializeTravelPlannerAnalysis() {
        final var usageModelPath = Paths.get("models", "TravelPlanner", "travelPlanner.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "TravelPlanner", "travelPlanner.allocation")
            .toString();

        travelPlannerAnalysis = new StandalonePCMDataFlowConfidentialtyAnalysis(TEST_MODEL_PROJECT_NAME,
                Activator.class, usageModelPath, allocationPath);

        travelPlannerAnalysis.initalizeAnalysis();
    }
}
