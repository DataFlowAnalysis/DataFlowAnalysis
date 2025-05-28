package org.dataflowanalysis.examplemodels.results.dfd.models;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class BranchingResult implements DFDExampleModelResult {
    @Override
    public String getModelName() {
        return "Branching";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .neverFlows()
                .toVertex()
                .with((vertex) -> vertex.getAllVertexCharacteristics()
                        .isEmpty())
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
}
