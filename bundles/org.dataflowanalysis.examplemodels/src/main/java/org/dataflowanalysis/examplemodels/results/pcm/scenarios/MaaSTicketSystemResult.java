package org.dataflowanalysis.examplemodels.results.pcm.scenarios;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;

public class MaaSTicketSystemResult implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "MaaSTicketSystem";
    }

    @Override
    public String getFileName() {
        return "MaaS";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        constraints.add(new ConstraintDSL().fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "MaliciousActor")
                .create());
        constraints.add(new ConstraintDSL().ofData()
                .withLabel("DataType", "LoginData")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "Customer")
                .create());
        constraints.add(new ConstraintDSL().ofData()
                .withLabel("Origin", "Leaked")
                .neverFlows()
                .toVertex()
                .create());
        return constraints;
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of();
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
