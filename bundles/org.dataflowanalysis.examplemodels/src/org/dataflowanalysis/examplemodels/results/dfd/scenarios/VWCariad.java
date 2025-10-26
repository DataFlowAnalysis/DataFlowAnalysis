package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class VWCariad implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "VWCariad";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("DataSensitivity", "Confidential")
                .neverFlows()
                .toVertex()
                .withCharacteristic("EndpointConfiguration", "Public")
                .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataSensitivity", "NonAnonymized")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("DataBaseType", List.of("AWSBucket", "AzureDataLake"))
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("RequestSensitivity", "Confidential")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("EndpointConfiguration", "Public")
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataBaseToken", "AWSToken")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("EndpointConfiguration", "Public")
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("DataEncryption", "NonEncrypted")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("DataBaseType", List.of("AWSBucket", "AzureDataLake"))
                        .create(),

                new ConstraintDSL().ofData()
                        .withLabel("UserClearance", "NonAuthenticated")
                        .neverFlows()
                        .toVertex()
                        .withCharacteristic("CarControlUnits", "ECU")
                        .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(22, new DFDIdentifier("poscub"), List.of(new ExpectedCharacteristic("EndpointConfiguration", "Public")),
                Map.of("xa0por", List.of(new ExpectedCharacteristic("DataSensitivity", "Confidential"),
                        new ExpectedCharacteristic("RequestSensitivity", "Confidential"), new ExpectedCharacteristic("DataBaseToken", "AWSToken"),
                        new ExpectedCharacteristic("DataEncryption", "NonEncrypted")))));

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
