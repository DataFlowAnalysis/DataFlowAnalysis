package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACContactSMSViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-ContactSMS-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	return List.of(new ConstraintDSL().ofData()
    			.withoutLabel("AccessRights", "Receiver")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("Role", "Receiver")
    			.create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
        		new ExpectedViolation(3, new DFDIdentifier("ztul2"),
        				List.of(new ExpectedCharacteristic("Role", "Receiver")),
        				Map.of("9ru4y", 
        						List.of(new ExpectedCharacteristic("AccessRights", "User")))),
        		new ExpectedViolation(3, new DFDIdentifier("6oilhg"),
                		List.of(new ExpectedCharacteristic("Role", "Receiver")),
                				Map.of("e45gpj",
                						List.of(new ExpectedCharacteristic("AccessRights", "User")))));	
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