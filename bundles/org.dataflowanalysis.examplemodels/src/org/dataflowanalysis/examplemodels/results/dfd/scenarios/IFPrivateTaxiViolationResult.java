package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class IFPrivateTaxiViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "IF-PrivateTaxi-violation";
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
    	return List.of(
    			new ExpectedViolation(45, 
    					new DFDIdentifier("szok6"), 
    						List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")), 
    						Map.of("l9ktzp", List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(45, 
    					new DFDIdentifier("voddy"), 
    						List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")), 
    						Map.of())
        		);
    }
    
    //Offending violations:[Violation in TFG: (Driver, wpwufj)
    //Violating vertices: [(store route, szok6), (Route Storage, voddy), (find proximity, f8hfrp)]
    //l9ktzp=[CriticalDataType.RouteDataType]
   
    @Override
    public String toString() {
        return this.getModelName();
    }
    
    @Override
    public String getFileName() {
    	return "diagram";
    }
}