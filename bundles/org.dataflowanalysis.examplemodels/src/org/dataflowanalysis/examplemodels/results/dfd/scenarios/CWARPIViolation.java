package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class CWARPIViolation implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "CWA/RPIViolation";
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
        return List.of(new ExpectedViolation(40, new DFDIdentifier("3dwb"),
                List.of(new ExpectedCharacteristic("Server", "CWAppServer"), new ExpectedCharacteristic("Cloud", "OTC")),
                Map.of("36il1r", List.of(), "2fhdu8j", List.of(new ExpectedCharacteristic("Information", "AnalyticData")), "vdzpwr", List.of(),
                        "whtd4m", List.of(new ExpectedCharacteristic("Identifiers", "RPI"), new ExpectedCharacteristic("Identifiers", "AEM")))));
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
