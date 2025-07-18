package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class MiniTwit_CaseStudy implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "MiniTwit_CaseStudy";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("UserData", "UnfollowData")
                .withLabel("UserData", "FollowData")
                .withLabel("UserData", "Message")
                .withLabel("UserData", "NewConsents")
                .neverFlows()
                .toVertex()
                .withoutCharacteristic("Decorator", "secure")
                .create(),

                new ConstraintDSL().ofData()
                        .withLabel("UserData", "UserMessages")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", "DisplayRelevantPosts")
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("UserData", "Ads")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", "GenerateRelevantMarketingEntities")
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("UserData", "FeedMessages")
                        .neverFlows()
                        .toVertex()
                        .withoutCharacteristic("ConsentedPurposes", "DisplayPublicTimeline")
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
