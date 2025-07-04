package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACABACViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-abac-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	return List.of(new ConstraintDSL().ofData()
    			.withLabel("DataStatus", "Celebrity")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("NodeRole", "Clerk")
    			.create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(2, new DFDIdentifier("370gkb"), 
				List.of(new ExpectedCharacteristic("NodeRole", "Clerk"), new ExpectedCharacteristic("NodeLocation", "Asia")), 
				Map.of("t2a5tp", 
						List.of(new ExpectedCharacteristic("DataOrigin", "Asia"), new ExpectedCharacteristic("DataStatus", "Regular"),
								new ExpectedCharacteristic("DataStatus", "Celebrity")),
						"bgccb", 
        				List.of(new ExpectedCharacteristic("DataOrigin", "Asia"), new ExpectedCharacteristic("DataStatus", "Regular"),
        						new ExpectedCharacteristic("DataStatus", "Celebrity")))),
						
		new ExpectedViolation(3, new DFDIdentifier("fis13o"), 
				List.of(new ExpectedCharacteristic("NodeRole", "Clerk"), new ExpectedCharacteristic("NodeLocation", "USA")), 
				Map.of("jrprc", 
						List.of(new ExpectedCharacteristic("DataOrigin", "USA"), new ExpectedCharacteristic("DataStatus", "Regular"),
								new ExpectedCharacteristic("DataStatus", "Celebrity")))));
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
