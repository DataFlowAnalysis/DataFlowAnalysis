package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class CWAPersonalDataViolation implements DFDExampleModelResult {

    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "CWAPersonalDataViolation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("Identifiers","RPI")
                .withLabel("Identifiers","TEK")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Server", "CWApp")
                .withCharacteristic("Server", "CWAppServer")
                .create(),
                
                new ConstraintDSL().ofData()
                .withLabel("Identifiers","PersonalData")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Server","VerificationServer")
                .create(),
        		
        		new ConstraintDSL().ofData()
                .withLabel("Identifiers","PersonalData")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Server", "CWApp")
                .withCharacteristic("Server","TestResultServer")
                .withCharacteristic("Server","DDServer")
                .withCharacteristic("Server","CWAppServer")
                .create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
    	return List.of(new ExpectedViolation(1, new DFDIdentifier("oz1nrf"),
        		List.of(new ExpectedCharacteristic("Server", "VerificationServer"), new ExpectedCharacteristic("Cloud","OTC")), 
        		Map.of("rly6k", List.of(new ExpectedCharacteristic("Identifiers", "PersonalData")))));
    			
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

