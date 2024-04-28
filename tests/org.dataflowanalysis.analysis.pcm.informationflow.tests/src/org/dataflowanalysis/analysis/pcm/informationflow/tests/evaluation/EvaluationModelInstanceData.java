package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.nio.file.Path;

public record EvaluationModelInstanceData(Path usagePath, Path allocationPath, Path nodePath, EvaluationSpecificationType specificationType,
        boolean violation) {

}
