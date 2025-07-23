package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class Hippa_CaseStudy implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "Hippa_CaseStudy";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("DataType", List.of("ConsentSettings", "NewConsentSettings", "IndexRecords"))
                .neverFlows()
                .toVertex()
                .withoutCharacteristic("Decorator", "secure")
                .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataType", "IndexRecords")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", "ViewRecords")
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
