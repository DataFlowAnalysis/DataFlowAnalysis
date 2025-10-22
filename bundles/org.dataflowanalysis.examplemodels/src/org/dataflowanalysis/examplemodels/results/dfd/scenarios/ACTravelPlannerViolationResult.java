package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACTravelPlannerViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-TravelPlanner-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("Levels", "User")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "Airline")
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
                new ExpectedViolation(0, new DFDIdentifier("djba98"), List.of(new ExpectedCharacteristic("Role", "Airline")),
                        Map.of("l4xfbo", List.of(new ExpectedCharacteristic("Levels", "UserAirlineAgency")), "70i8yq",
                                List.of(new ExpectedCharacteristic("Levels", "UserAirline"), new ExpectedCharacteristic("Levels", "User")))),
                new ExpectedViolation(0, new DFDIdentifier("pnz9vg"), List.of(new ExpectedCharacteristic("Role", "Airline")),
                        Map.of("u32kc", List.of(new ExpectedCharacteristic("Levels", "UserAirlineAgency"),
                                new ExpectedCharacteristic("Levels", "UserAirline"), new ExpectedCharacteristic("Levels", "User")))));
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