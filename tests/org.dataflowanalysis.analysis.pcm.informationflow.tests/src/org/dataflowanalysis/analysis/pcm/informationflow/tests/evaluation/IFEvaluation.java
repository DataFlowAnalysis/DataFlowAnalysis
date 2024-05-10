package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class IFEvaluation {

    private static final Logger logger = Logger.getLogger(IFEvaluation.class);

    private static List<String> resultStrings = new ArrayList<>();

    public static void main(String[] args) {

        resultStrings.add("----### structural models ###----");
        resultStrings.add("---- generated ----");

        evaluateModel(EvaluationModels.explicitDirectModel);
        evaluateModel(EvaluationModels.explicitProcessedModel);
        evaluateModel(EvaluationModels.explicitSurroundedModel);

        evaluateModel(EvaluationModels.implicitSimpleModel);
        evaluateModel(EvaluationModels.implicitNestedModel);
        evaluateModel(EvaluationModels.implicitSurroundedModel);
        evaluateModel(EvaluationModels.implicitElseModel);

        resultStrings.add("---- manually specified ----");

        evaluateModel(EvaluationModels.explicitDirectModelManuallySpecified);
        evaluateModel(EvaluationModels.explicitProcessedModelManuallySpecified);
        evaluateModel(EvaluationModels.explicitSurroundedModelManuallySpecified);

        evaluateModel(EvaluationModels.implicitSimpleModelManuallySpecified);
        evaluateModel(EvaluationModels.implicitNestedModelManuallySpecified);
        evaluateModel(EvaluationModels.implicitSurroundedModelManuallySpecified);
        evaluateModel(EvaluationModels.implicitElseModelManuallySpecified);

        resultStrings.add("----### case study models ###----");
        resultStrings.add("---- generated ----");

        evaluateModel(EvaluationModels.contactSMSModel);
        evaluateModel(EvaluationModels.distanceTrackerModel);
        evaluateModel(EvaluationModels.friendMapModel);
        evaluateModel(EvaluationModels.hospitalModel);
        // evaluateModel(EvaluationModels.JPMailModel); //Contains Connectors for DataChannels
        evaluateModel(EvaluationModels.travelPlannerModel);
        // evaluateModel(EvaluationModels.webRTCModel); //Contains Connectors for DataChannels

        resultStrings.add("---- manually specified ----");

        evaluateModel(EvaluationModels.contactSMSModelManuallySpecified);
        evaluateModel(EvaluationModels.distanceTrackerModelManuallySpecified);
        evaluateModel(EvaluationModels.friendMapModelManuallySpecified);
        evaluateModel(EvaluationModels.hospitalModelManuallySpecified);
        // evaluateModel(EvaluationModels.JPMailModelManuallySpecified); //Contains Connectors for DataChannels
        evaluateModel(EvaluationModels.travelPlannerModelManuallySpecified);
        // evaluateModel(EvaluationModels.webRTCModelManuallySpecified); //Contains Connectors for DataChannels

        printResults();
    }

    private static void evaluateModel(EvaluationModelData model) {
        Map<IFEvaluationClassification, Integer> classificationToAmount = ModelExecutionUtils.executeAndClassifyModel(model);

        recordEvaluationResultForModel(model, classificationToAmount);
    }

    private static void recordEvaluationResultForModel(EvaluationModelData model, Map<IFEvaluationClassification, Integer> classificationToAmount) {

        resultStrings.add(String.format("%1$-20s | TP=%2$s, TN=%3$s, FP=%4$s, FN=%5$s", model.modelName(),
                classificationToAmount.get(IFEvaluationClassification.TruePositive),
                classificationToAmount.get(IFEvaluationClassification.TrueNegative),
                classificationToAmount.get(IFEvaluationClassification.FalsePositive),
                classificationToAmount.get(IFEvaluationClassification.FalseNegative)));
    }

    private static void printResults() {
        for (String resultLine : resultStrings) {
            logger.info(resultLine);
        }
    }

}
