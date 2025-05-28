package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class SimpleOnlineShopResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "SimpleOnlineShop";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {

        return List.of(new ConstraintDSL().ofData()
                .withLabel("Sensitivity", "Personal")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Location", "nonEU")
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(1, new DFDIdentifier("_U27Lor6CEe6fAKdvyu1GEg"),
                List.of(new ExpectedCharacteristic("Location", "nonEU")), Map.of("_448GwMBCEe62ZOq30ePU7Q",
                        List.of(new ExpectedCharacteristic("Sensitivity", "Personal"), new ExpectedCharacteristic("Encryption", "Encrypted")))));
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
