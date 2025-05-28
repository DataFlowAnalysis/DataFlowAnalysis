package org.dataflowanalysis.examplemodels.results.pcm.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;

/**
 * FIXME: The CoCar Scenario does not have any constraint and therefore does not have expected violations
 */
public class CoCarResult implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "CoCarNextGen";
    }

    @Override
    public String getFileName() {
        return "AudiA6C8";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of();
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of();
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
