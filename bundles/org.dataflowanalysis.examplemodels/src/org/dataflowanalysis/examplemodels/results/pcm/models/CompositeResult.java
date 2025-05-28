package org.dataflowanalysis.examplemodels.results.pcm.models;

import java.util.ArrayList;
import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.UserPCMVertex;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;

public class CompositeResult implements PCMExampleModelResult {
    @Override
    public String getModelName() {
        return "CompositeCharacteristics";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        constraints.add(new ConstraintDSL().ofData()
                .neverFlows()
                .toVertex()
                .with((vertex) -> vertex instanceof UserPCMVertex<?> && vertex.getAllVertexCharacteristics()
                        .size() != 1)
                .with((vertex) -> vertex instanceof SEFFPCMVertex<?> && vertex.getAllVertexCharacteristics()
                        .size() != 3)
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
