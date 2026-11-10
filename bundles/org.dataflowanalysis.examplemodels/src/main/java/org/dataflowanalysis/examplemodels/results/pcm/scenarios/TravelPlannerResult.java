package org.dataflowanalysis.examplemodels.results.pcm.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;
import org.dataflowanalysis.examplemodels.results.pcm.PCMIdentifier;

public class TravelPlannerResult implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "TravelPlanner";
    }

    @Override
    public String getFileName() {
        return "travelPlanner";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("GrantedRoles", ConstraintVariable.of("grantedRoles"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("AssignedRoles", ConstraintVariable.of("assignedRoles"))
                .where()
                .isNotEmpty(ConstraintVariable.of("grantedRoles"))
                .isNotEmpty(ConstraintVariable.of("assignedRoles"))
                .isEmpty(Intersection.of(ConstraintVariable.of("grantedRoles"), ConstraintVariable.of("assignedRoles")))
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(0, PCMIdentifier.of("_vorK8fVeEeuMKba1Qn68bg", true),
                List.of(new ExpectedCharacteristic("AssignedRoles", "Airline")),
                Map.of("flight", List.of(new ExpectedCharacteristic("GrantedRoles", "User"), new ExpectedCharacteristic("GrantedRoles", "Airline")),
                        "ccd", List.of(new ExpectedCharacteristic("GrantedRoles", "User")))),
                new ExpectedViolation(0, PCMIdentifier.of("_7HCu4PViEeuMKba1Qn68bg", false),
                        List.of(new ExpectedCharacteristic("AssignedRoles", "Airline")),
                        Map.of("flight",
                                List.of(new ExpectedCharacteristic("GrantedRoles", "User"), new ExpectedCharacteristic("GrantedRoles", "Airline")),
                                "ccd", List.of(new ExpectedCharacteristic("GrantedRoles", "User")))),
                new ExpectedViolation(0, PCMIdentifier.of("_vorK8vVeEeuMKba1Qn68bg", false),
                        List.of(new ExpectedCharacteristic("AssignedRoles", "Airline")),
                        Map.of("flight",
                                List.of(new ExpectedCharacteristic("GrantedRoles", "User"), new ExpectedCharacteristic("GrantedRoles", "Airline")),
                                "ccd", List.of(new ExpectedCharacteristic("GrantedRoles", "User")), "RETURN",
                                List.of(new ExpectedCharacteristic("GrantedRoles", "User"), new ExpectedCharacteristic("GrantedRoles", "Airline")))));
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
