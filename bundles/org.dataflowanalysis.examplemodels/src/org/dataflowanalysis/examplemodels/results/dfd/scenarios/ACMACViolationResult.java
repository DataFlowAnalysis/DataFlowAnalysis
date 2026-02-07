package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACMACViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-MilitaryAircraftController-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("ClassificationLevel", "Classified")
                .neverFlows()
                .toVertex()
                .withCharacteristic("ClearanceLevel", "Unclassified")
                .create(),
                new ConstraintDSL().ofData()
                        .withLabel("ClassificationLevel", "Secret")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("ClearanceLevel", "Classified")
                        .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
                new ExpectedViolation(2, new DFDIdentifier("6xeidb"), List.of(new ExpectedCharacteristic("ClearanceLevel", "Classified")),
                        Map.of("m1fco", List.of(new ExpectedCharacteristic("ClassificationLevel", "Classified")), "3vt6d",
                                List.of(new ExpectedCharacteristic("ClassificationLevel", "Secret")), "5bphw9",
                                List.of(new ExpectedCharacteristic("ClassificationLevel", "Unclassified")), "45xcd",
                                List.of(new ExpectedCharacteristic("ClassificationLevel", "Classified")))),
                new ExpectedViolation(2, new DFDIdentifier("g00dna"), List.of(new ExpectedCharacteristic("ClearanceLevel", "Classified")),
                        Map.of("e0otvg", List.of(new ExpectedCharacteristic("ClassificationLevel", "Secret")), "h0xebf",
                                List.of(new ExpectedCharacteristic("ClassificationLevel", "Classified")))));
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