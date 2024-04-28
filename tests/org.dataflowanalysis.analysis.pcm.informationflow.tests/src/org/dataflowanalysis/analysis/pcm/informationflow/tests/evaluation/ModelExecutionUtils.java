package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.informationflow.IFPCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionMode;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.IFTestsActivator;
import org.dataflowanalysis.analysis.pcm.informationflow.tests.ModelCreationTestUtils;

public class ModelExecutionUtils {

    private ModelExecutionUtils() {
    }

    public static Map<IFEvaluationClassification, Integer> executeAndClassifyModel(EvaluationModelData model) {
        Map<IFEvaluationClassification, Integer> classificationToAmount = new HashMap<>();
        for (var classification : IFEvaluationClassification.values()) {
            classificationToAmount.put(classification, 0);
        }

        for (EvaluationModelInstanceData modelInstance : model.modelInstances()) {
            boolean foundViolation = ModelExecutionUtils.executeAnalysis(modelInstance, model.violationCondition());

            var classification = IFEvaluationClassification.classify(modelInstance.violation(), foundViolation);

            classificationToAmount.put(classification, classificationToAmount.get(classification) + 1);
        }

        return classificationToAmount;
    }

    public static boolean executeAnalysis(EvaluationModelInstanceData modelInstanceData, Predicate<? super AbstractVertex<?>> condition) {
        if (modelInstanceData.specificationType()
                .equals(EvaluationSpecificationType.AllConfidentialityCharacterisations)) {
            return executeNormalAnalysis(modelInstanceData, condition);
        } else {
            return executeInformationflowAnalysis(modelInstanceData, condition);
        }
    }

    public static boolean executeNormalAnalysis(EvaluationModelInstanceData modelInstanceData, Predicate<? super AbstractVertex<?>> condition) {
        var analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(ModelCreationTestUtils.TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(IFTestsActivator.class)
                .useUsageModel(modelInstanceData.usagePath()
                        .toString())
                .useAllocationModel(modelInstanceData.allocationPath()
                        .toString())
                .useNodeCharacteristicsModel(modelInstanceData.nodePath()
                        .toString())
                .build();
        return executeAnalysis(analysis, condition);
    }

    public static boolean executeInformationflowAnalysis(EvaluationModelInstanceData modelInstanceData,
            Predicate<? super AbstractVertex<?>> condition) {
        var analysis = new IFPCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(ModelCreationTestUtils.TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(IFTestsActivator.class)
                .useUsageModel(modelInstanceData.usagePath()
                        .toString())
                .useAllocationModel(modelInstanceData.allocationPath()
                        .toString())
                .useNodeCharacteristicsModel(modelInstanceData.nodePath()
                        .toString())
                .setConsiderImplicitFlow(true)
                .setExtractionMode(IFPCMExtractionMode.PreferConsider)
                .build();
        return executeAnalysis(analysis, condition);
    }

    private static boolean executeAnalysis(PCMDataFlowConfidentialityAnalysis analysis, Predicate<? super AbstractVertex<?>> condition) {
        analysis.initializeAnalysis();
        FlowGraph flowGraph = analysis.findFlowGraph();
        FlowGraph propagatedFlowGraph = analysis.evaluateFlowGraph(flowGraph);

        boolean containsViolation = false;
        for (var parialFlowGraph : propagatedFlowGraph.getPartialFlowGraphs()) {
            var violations = analysis.queryDataFlow(parialFlowGraph, condition);
            if (!violations.isEmpty()) {
                containsViolation = true;
            }
        }
        return containsViolation;
    }

}
