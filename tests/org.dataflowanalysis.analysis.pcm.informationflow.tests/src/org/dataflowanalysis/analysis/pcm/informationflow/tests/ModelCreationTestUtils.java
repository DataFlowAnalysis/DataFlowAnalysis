package org.dataflowanalysis.analysis.pcm.informationflow.tests;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysisBuilder;

public class ModelCreationTestUtils {

    public static final String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.pcm.informationflow.tests";

    private ModelCreationTestUtils() {
    }

    public static IFPCMDataFlowConfidentialityAnalysis createBranchingCallAnalysis() {
        return createAnalysisFromModelName("BranchingCalls");
    }

    public static IFPCMDataFlowConfidentialityAnalysis createSwappedCallsAnalysis() {
        return createAnalysisFromModelName("SwappedCallsModel");
    }

    public static IFPCMDataFlowConfidentialityAnalysis createAnalysisFromModelName(String modelName) {
        final var usageModelPath = Paths.get("models", modelName, "default.usagemodel");
        final var allocationPath = Paths.get("models", modelName, "default.allocation");
        final var nodeCharacteristicsPath = Paths.get("models", modelName, "default.nodecharacteristics");
        return initializeAnalysis(usageModelPath, allocationPath, nodeCharacteristicsPath);
    }

    private static IFPCMDataFlowConfidentialityAnalysis initializeAnalysis(Path usagePath, Path allocationPath, Path nodePath) {
        return initializeAnalysis(TEST_MODEL_PROJECT_NAME, usagePath, allocationPath, nodePath);
    }

    private static IFPCMDataFlowConfidentialityAnalysis initializeAnalysis(String testModelProjectName, Path usagePath, Path allocationPath,
            Path nodePath) {
        IFPCMDataFlowConfidentialityAnalysis analysis = new IFPCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(testModelProjectName)
                .usePluginActivator(IFTestsActivator.class)
                .useUsageModel(usagePath.toString())
                .useAllocationModel(allocationPath.toString())
                .useNodeCharacteristicsModel(nodePath.toString())
                .build();
        analysis.initializeAnalysis();
        analysis.setLoggerLevel(Level.ALL);
        return analysis;
    }
}
