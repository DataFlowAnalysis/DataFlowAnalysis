package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;

public class IFPrivateTaxiNoViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "IF-PrivateTaxi-no-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("CriticalDataType", "RouteDataType")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Entity", "PrivateTaxi")
                .create(),
                new ConstraintDSL().ofData()
                        .withLabel("CriticalDataType", "ContactInformation")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("Entity", "CalcDistanceService")
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

    @Override
    public String getFileName() {
        return "diagram";
    }
}