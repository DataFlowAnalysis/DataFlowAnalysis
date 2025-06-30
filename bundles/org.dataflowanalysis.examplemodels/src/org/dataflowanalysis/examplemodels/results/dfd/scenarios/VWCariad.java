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
        return "vwcariad";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("DataSensitivity","confidential")
                .neverFlows()
                .toVertex()
                .withCharacteristic("EndpointConfiguration", "public")
                .create(),
                
                new ConstraintDSL().ofData()
                .withLabel("DataSensitivity","non_anonymized")
                .neverFlows()
                .toVertex()
                .withCharacteristic("DataBaseType", "AWSBucket")
                .withCharacteristic("DataBaseType", "AzureDataLake")
                .create(),

                new ConstraintDSL().ofData()
                .withLabel("RequestSensitivity","confidential")
                .neverFlows()
                .toVertex()
                .withCharacteristic("EndpointConfiguration", "public")
                .create(),

                new ConstraintDSL().ofData()
                .withLabel("DataBaseToken","AWSToken")
                .neverFlows()
                .toVertex()
                .withCharacteristic("EndpointConfiguration", "public")
                .create(),

                new ConstraintDSL().ofData()
                .withLabel("DataEncryption","nonEncrypted")
                .neverFlows()
                .toVertex()
                .withCharacteristic("DataBaseType", "AWSBucket")
                .withCharacteristic("DataBaseType", "AzureDataLake")
                .create(),

                new ConstraintDSL().ofData()
                .withLabel("UserClearance","non_authenticated")
                .neverFlows()
                .toVertex()
                .withCharacteristic("CarControlUnits", "ECU")
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(22, new DFDIdentifier("poscub"),
        		List.of(new ExpectedCharacteristic("EndpointConfiguration", "public")), 
        		Map.of("xa0por", List.of(new ExpectedCharacteristic("DataSensitivity","confidential"),
        								 new ExpectedCharacteristic("RequestSensitivity","confidential"),
        								 new ExpectedCharacteristic("DataBaseToken","AWSToken"),
        								 new ExpectedCharacteristic("DataEncryption","nonEncrypted")))));


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

