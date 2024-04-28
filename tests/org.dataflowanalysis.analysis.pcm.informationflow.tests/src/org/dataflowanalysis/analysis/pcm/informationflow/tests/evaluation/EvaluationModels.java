package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.nio.file.Path;
import java.util.List;

public class EvaluationModels {

    private static final String models = "models";
    private static final String evaluation_flows = "evaluation-flows";

    private static final String defaultUsage = "default.usagemodel";
    private static final String defaultAllocation = "default.allocation";
    private static final String defaultNodecharacteristics = "default.nodecharacteristics";

    private static final String explicit_direct = "explicit-direct";
    private static final String explicit_processed = "explicit-processed";
    private static final String explicit_surrounded = "explicit-surrounded";
    private static final String implicit_simple = "implicit-simple";
    private static final String implicit_nested = "implicit-nested";
    private static final String implicit_surrounded = "implicit-surrounded";
    private static final String implicit_else = "implicit-else";

    /*
     * explicit-direct
     */

    public static final EvaluationModelInstanceData explicitDirectNoViolation = createEvaluationModelDataForFlowModel(explicit_direct, "noViolation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitDirectViolation = createEvaluationModelDataForFlowModel(explicit_direct, "violation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitDirectModel = new EvaluationModelData(explicit_direct,
            List.of(explicitDirectNoViolation, explicitDirectViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData explicitDirectNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            explicit_direct, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitDirectViolationManuallySpecified = createEvaluationModelDataForFlowModel(explicit_direct,
            "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitDirectModelManuallySpecified = new EvaluationModelData(explicit_direct,
            List.of(explicitDirectNoViolationManuallySpecified, explicitDirectViolationManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * explicit-processed
     */

    public static final EvaluationModelInstanceData explicitProcessedNoViolation = createEvaluationModelDataForFlowModel(explicit_processed,
            "noViolation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitProcessedViolation = createEvaluationModelDataForFlowModel(explicit_processed,
            "violation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitProcessedModel = new EvaluationModelData(explicit_processed,
            List.of(explicitProcessedNoViolation, explicitProcessedViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData explicitProcessedNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            explicit_processed, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitProcessedViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            explicit_processed, "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitProcessedModelManuallySpecified = new EvaluationModelData(explicit_processed,
            List.of(explicitProcessedNoViolationManuallySpecified, explicitProcessedViolationManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * explicit-surrounded
     */

    public static final EvaluationModelInstanceData explicitSurroundedNoViolation = createEvaluationModelDataForFlowModel(explicit_surrounded,
            "noViolation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitSurroundedViolation = createEvaluationModelDataForFlowModel(explicit_surrounded,
            "violation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitSurroundedModel = new EvaluationModelData(explicit_surrounded,
            List.of(explicitSurroundedNoViolation, explicitSurroundedViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData explicitSurroundedNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            explicit_surrounded, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData explicitSurroundedViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            explicit_surrounded, "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData explicitSurroundedModelManuallySpecified = new EvaluationModelData(explicit_surrounded,
            List.of(explicitSurroundedNoViolationManuallySpecified, explicitSurroundedViolationManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * implicit-simple
     */

    public static final EvaluationModelInstanceData implicitSimpleNoViolation = createEvaluationModelDataForFlowModel(implicit_simple, "noViolation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitSimpleViolation = createEvaluationModelDataForFlowModel(implicit_simple, "violation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitSimpleModel = new EvaluationModelData(implicit_simple,
            List.of(implicitSimpleNoViolation, implicitSimpleViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData implicitSimpleNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_simple, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitSimpleViolationManuallySpecified = createEvaluationModelDataForFlowModel(implicit_simple,
            "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitSimpleModelManuallySpecified = new EvaluationModelData(implicit_simple,
            List.of(implicitSimpleNoViolationManuallySpecified, implicitSimpleViolationManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * implicit-nested
     */

    public static final EvaluationModelInstanceData implicitNestedNoViolation = createEvaluationModelDataForFlowModel(implicit_nested, "noViolation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitNestedViolationInner = createEvaluationModelDataForFlowModel(implicit_nested,
            "violationInner", EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);
    public static final EvaluationModelInstanceData implicitNestedViolationOuter = createEvaluationModelDataForFlowModel(implicit_nested,
            "violationOuter", EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitNestedModel = new EvaluationModelData(implicit_nested,
            List.of(implicitNestedNoViolation, implicitNestedViolationInner, implicitNestedViolationOuter),
            EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData implicitNestedNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_nested, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitNestedViolationInnerManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_nested, "violationInner-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);
    public static final EvaluationModelInstanceData implicitNestedViolationOuterManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_nested, "violationOuter-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitNestedModelManuallySpecified = new EvaluationModelData(implicit_nested,
            List.of(implicitNestedNoViolationManuallySpecified, implicitNestedViolationInnerManuallySpecified,
                    implicitNestedViolationOuterManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * implicit-surrounded
     */

    public static final EvaluationModelInstanceData implicitSurroundedNoViolation = createEvaluationModelDataForFlowModel(implicit_surrounded,
            "noViolation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitSurroundedViolation = createEvaluationModelDataForFlowModel(implicit_surrounded,
            "violation", EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitSurroundedModel = new EvaluationModelData(implicit_surrounded,
            List.of(implicitSurroundedNoViolation, implicitSurroundedViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData implicitSurroundedNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_surrounded, "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitSurroundedViolationManuallySpecified = createEvaluationModelDataForFlowModel(
            implicit_surrounded, "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitSurroundedModelManuallySpecified = new EvaluationModelData(implicit_surrounded,
            List.of(implicitSurroundedNoViolationManuallySpecified, implicitSurroundedViolationManuallySpecified),
            EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * implicit-else
     */

    public static final EvaluationModelInstanceData implicitElseNoViolation = createEvaluationModelDataForFlowModel(implicit_else, "noViolation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitElseViolation = createEvaluationModelDataForFlowModel(implicit_else, "violation",
            EvaluationSpecificationType.NeededConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitElseModel = new EvaluationModelData(implicit_else,
            List.of(implicitElseNoViolation, implicitElseViolation), EvaluationModelConditionUtils::highFlowsToLow);

    public static final EvaluationModelInstanceData implicitElseNoViolationManuallySpecified = createEvaluationModelDataForFlowModel(implicit_else,
            "noViolation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, false);
    public static final EvaluationModelInstanceData implicitElseViolationManuallySpecified = createEvaluationModelDataForFlowModel(implicit_else,
            "violation-cvcs", EvaluationSpecificationType.AllConfidentialityCharacterisations, true);

    public static final EvaluationModelData implicitElseModelManuallySpecified = new EvaluationModelData(implicit_else,
            List.of(implicitElseNoViolationManuallySpecified, implicitElseViolationManuallySpecified), EvaluationModelConditionUtils::highFlowsToLow);

    /*
     * Helper methods
     */

    private static EvaluationModelInstanceData createEvaluationModelDataForFlowModel(String flowModel, String flowModelling,
            EvaluationSpecificationType specificationType, boolean containsViolation) {
        return new EvaluationModelInstanceData(Path.of(models, evaluation_flows, flowModel, flowModelling, defaultUsage),
                Path.of(models, evaluation_flows, flowModel, flowModelling, defaultAllocation),
                Path.of(models, evaluation_flows, flowModel, flowModelling, defaultNodecharacteristics), specificationType, containsViolation);
    }

}
