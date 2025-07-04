package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class IFTravelPlannerViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "IF-TravelPlanner-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	return List.of(new ConstraintDSL().ofData()
    			.withLabel("ClassificationLevel", "User")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("ClearanceLevel", "UserAirline")
    			.create(),
    			
    			new ConstraintDSL().ofData()
    			.withLabel("ClassificationLevel", "User")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("ClearanceLevel", "UserAirlineAgency")
    			.create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
        		new ExpectedViolation(2, new DFDIdentifier("djba98"),
				List.of(new ExpectedCharacteristic("ClearanceLevel", "UserAirline")),
				Map.of("l4xfbo", 
							List.of(new ExpectedCharacteristic("ClassificationLevel", "UserAirlineAgency")), 
						"70i8yq", 
						List.of(new ExpectedCharacteristic("ClassificationLevel", "UserAirline"),
							new ExpectedCharacteristic("ClassificationLevel", "User")))),
        		new ExpectedViolation(2, new DFDIdentifier("pnz9vg"),
        				List.of(new ExpectedCharacteristic("ClearanceLevel", "UserAirline")),
        				Map.of("u32kc", 
    							List.of(new ExpectedCharacteristic("ClassificationLevel", "UserAirlineAgency"), 
    									new ExpectedCharacteristic("ClassificationLevel", "UserAirline"),
    									new ExpectedCharacteristic("ClassificationLevel", "User")))));
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