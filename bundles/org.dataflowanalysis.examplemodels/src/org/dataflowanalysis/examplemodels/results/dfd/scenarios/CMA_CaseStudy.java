package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class CMA_CaseStudy implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "CMA_CaseStudy";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("DataType",
                        List.of("AcceptedPaper", "ConsentSettings", "ReviewedPaper", "CandidateReviewers", "Manuscript", "NewConsents", "Purpose"))
                .neverFlows()
                .toVertex()
                .withoutCharacteristic("Decorator", "Secure")
                .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataType", "CandidateReviewers")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", "AssignReviewer")
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataType", "AcceptedPaper")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", List.of("ViewPaper", "RecommendPapers"))
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
