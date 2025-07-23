package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class CWANoViolation implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "CWANoViolation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("Identifiers", List.of("RPI", "TEK"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("Server", List.of("CWApp", "CWAppServer"))
                .create(),

                new ConstraintDSL().ofData()
                        .withLabel("Identifiers", "PersonalData")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("Server", List.of("CWApp", "VerificationServer", "TestResultServer", "DDServer", "CWAppServer"))
                        .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of();
    }

    @Override
    public String toString() {
        return this.getModelName();
    }

    @Override
    public String getFileName() {
        return "diagram";
    }
}
