package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACDACViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-DAC-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withoutLabel("Read", "IndexingBot")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Identity", "IndexingBot")
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(3, new DFDIdentifier("fagty"), List.of(new ExpectedCharacteristic("Identity", "IndexingBot")),
                Map.of("h5c7l",
                        List.of(new ExpectedCharacteristic("TraversedNodes", "mother"), new ExpectedCharacteristic("TraversedNodes", "addPicture"),
                                new ExpectedCharacteristic("TraversedNodes", "pictureStorage"), new ExpectedCharacteristic("Read", "Aunt"),
                                new ExpectedCharacteristic("TraversedNodes", "readPicture")))));
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