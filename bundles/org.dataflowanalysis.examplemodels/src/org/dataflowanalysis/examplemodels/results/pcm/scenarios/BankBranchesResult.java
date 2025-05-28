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

public class BankBranchesResult implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "BankBranches";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("Status", "Celebrity")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "Clerk")
                .create(),
                new ConstraintDSL().ofData()
                        .withLabel("Origin", ConstraintVariable.of("OriginLocation"))
                        .fromNode()
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("Role", "Clerk")
                        .withCharacteristic("Location", ConstraintVariable.of("DestinationLocation"))
                        .where()
                        .isEmpty(Intersection.of(ConstraintVariable.of("OriginLocation"), ConstraintVariable.of("DestinationLocation")))
                        .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
                new ExpectedViolation(0, PCMIdentifier.of("_NTFQ0D6xEeuVUal8mM_jUA", false),
                        List.of(new ExpectedCharacteristic("Location", "USA"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("RETURN", List.of(new ExpectedCharacteristic("Status", "Celebrity")), "customerName",
                                List.of(new ExpectedCharacteristic("Status", "Regular"), new ExpectedCharacteristic("Origin", "USA")), "customer",
                                List.of(new ExpectedCharacteristic("Status", "Regular"), new ExpectedCharacteristic("Origin", "USA")))),
                new ExpectedViolation(0, PCMIdentifier.of("_KU1XAD6xEeuVUal8mM_jUA", true),
                        List.of(new ExpectedCharacteristic("Location", "USA"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("customerName", List.of(ExpectedCharacteristic.of("Origin", "USA"), ExpectedCharacteristic.of("Status", "Regular")),
                                "customer", List.of(ExpectedCharacteristic.of("Status", "Celebrity")))),
                new ExpectedViolation(0, PCMIdentifier.of("_KU1XAD6xEeuVUal8mM_jUA", false),
                        List.of(new ExpectedCharacteristic("Location", "USA"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("RETURN",
                                List.of(ExpectedCharacteristic.of("Origin", "USA"), ExpectedCharacteristic.of("Status", "Regular"),
                                        ExpectedCharacteristic.of("Status", "Celebrity")),
                                "customerName", List.of(ExpectedCharacteristic.of("Origin", "USA"), ExpectedCharacteristic.of("Status", "Regular")),
                                "customer", List.of(ExpectedCharacteristic.of("Status", "Celebrity")))),
                new ExpectedViolation(0, PCMIdentifier.of("_3Y4qYj6wEeuVUal8mM_jUA"),
                        List.of(new ExpectedCharacteristic("Location", "USA"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("creditLine",
                                List.of(ExpectedCharacteristic.of("Origin", "USA"), ExpectedCharacteristic.of("Status", "Regular"),
                                        ExpectedCharacteristic.of("Status", "Celebrity")),
                                "customerName", List.of(ExpectedCharacteristic.of("Origin", "USA"), ExpectedCharacteristic.of("Status", "Regular")),
                                "customer", List.of(ExpectedCharacteristic.of("Status", "Celebrity")))),
                new ExpectedViolation(2, PCMIdentifier.of("_JD3o8D6yEeuVUal8mM_jUA", false),
                        List.of(new ExpectedCharacteristic("Location", "Asia"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("RETURN", List.of(new ExpectedCharacteristic("Status", "Celebrity")), "customerName",
                                List.of(new ExpectedCharacteristic("Status", "Regular"), new ExpectedCharacteristic("Origin", "Asia")), "customer",
                                List.of(new ExpectedCharacteristic("Status", "Regular"), new ExpectedCharacteristic("Origin", "Asia")))),
                new ExpectedViolation(2, PCMIdentifier.of("_LxynwD6yEeuVUal8mM_jUA", true),
                        List.of(new ExpectedCharacteristic("Location", "Asia"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("customerName", List.of(ExpectedCharacteristic.of("Origin", "Asia"), ExpectedCharacteristic.of("Status", "Regular")),
                                "customer", List.of(ExpectedCharacteristic.of("Status", "Celebrity")))),
                new ExpectedViolation(2, PCMIdentifier.of("_LxynwD6yEeuVUal8mM_jUA", false),
                        List.of(new ExpectedCharacteristic("Location", "Asia"), new ExpectedCharacteristic("Role", "Clerk")),
                        Map.of("RETURN",
                                List.of(ExpectedCharacteristic.of("Origin", "Asia"), ExpectedCharacteristic.of("Status", "Regular"),
                                        ExpectedCharacteristic.of("Status", "Celebrity")),
                                "customerName", List.of(ExpectedCharacteristic.of("Origin", "Asia"), ExpectedCharacteristic.of("Status", "Regular")),
                                "customer", List.of(ExpectedCharacteristic.of("Status", "Celebrity")))));
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
