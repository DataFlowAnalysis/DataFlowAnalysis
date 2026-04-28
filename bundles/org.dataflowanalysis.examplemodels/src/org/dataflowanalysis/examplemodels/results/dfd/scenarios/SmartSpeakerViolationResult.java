package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class SmartSpeakerViolationResult implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "PurposeLimitationsPaper";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of();
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of();
    }
}
