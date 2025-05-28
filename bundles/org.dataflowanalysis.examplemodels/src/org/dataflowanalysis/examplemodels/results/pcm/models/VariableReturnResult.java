package org.dataflowanalysis.examplemodels.results.pcm.models;

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

public class VariableReturnResult implements PCMExampleModelResult {
    @Override
    public String getModelName() {
        return "VariableReturn";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("AssignedRole", ConstraintVariable.of("grantedRoles"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("AssignedRole", ConstraintVariable.of("assignedRoles"))
                .where()
                .isNotEmpty(ConstraintVariable.of("grantedRoles"))
                .isNotEmpty(ConstraintVariable.of("assignedRoles"))
                .isEmpty(Intersection.of(ConstraintVariable.of("grantedRoles"), ConstraintVariable.of("assignedRoles")))
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
                new ExpectedViolation(0, PCMIdentifier.of("_nOhAgILtEe2YyoqaKVkqog", false),
                        List.of(new ExpectedCharacteristic("AssignedRole", "User")),
                        Map.of("RETURN", List.of(new ExpectedCharacteristic("AssignedRole", "Admin")))),
                new ExpectedViolation(0, PCMIdentifier.of("_9M9DMoLsEe2YyoqaKVkqog", false),
                        List.of(new ExpectedCharacteristic("AssignedRole", "User")),
                        Map.of("data", List.of(new ExpectedCharacteristic("AssignedRole", "Admin")))));
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
