package org.dataflowanalysis.analysis.pcm.informationflow.tests.evaluation;

import java.util.List;
import java.util.function.Predicate;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public record EvaluationModelData(String modelName, List<EvaluationModelInstanceData> modelInstances,
        Predicate<? super AbstractVertex<?>> violationCondition) {

}
