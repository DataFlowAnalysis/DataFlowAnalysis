package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

public enum IFEvaluationClassification {
    TruePositive,
    FalsePositive,
    TrueNegative,
    FalseNegative;

    public static IFEvaluationClassification classify(boolean containsViolation, boolean foundViolation) {
        if (containsViolation && foundViolation) {
            return IFEvaluationClassification.TruePositive;
        } else if (!containsViolation && foundViolation) {
            return IFEvaluationClassification.FalsePositive;
        } else if (!containsViolation && !foundViolation) {
            return IFEvaluationClassification.TrueNegative;
        } else {
            return IFEvaluationClassification.FalseNegative;
        }
    }
}
