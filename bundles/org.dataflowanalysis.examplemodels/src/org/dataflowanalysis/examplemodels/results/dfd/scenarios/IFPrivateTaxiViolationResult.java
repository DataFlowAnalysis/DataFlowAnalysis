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
    	    	new ExpectedViolation(8, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(8, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(8, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(9, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(9, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(9, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(10, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(10, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(10, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(11, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(11, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(11, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(20, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(20, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(20, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(21, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(21, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(21, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(22, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(22, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(22, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(23, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(23, new DFDIdentifier("voddy"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("f0m37",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(23, new DFDIdentifier("f8hfrp"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("9sp5dx",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
    			new ExpectedViolation(29, new DFDIdentifier("szok6"),
    					List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
    					Map.of("brzxei",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "ContactInformation")),
    							"l9ktzp",
    							List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))));
    	
    }
    
    /**
     * List.of(
    	new ExpectedViolation(8, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(8, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(8, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(9, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(9, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(9, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(10, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(10, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(10, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(11, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(11, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(11, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(20, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(20, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpecnewtedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(20, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(21, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(21, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(21, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(22, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(22, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(22, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(23, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(23, new DFDIdentifier("voddy"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("f0m37",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(23, new DFDIdentifier("f8hfrp"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("9sp5dx",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))),
		new ExpectedViolation(29, new DFDIdentifier("szok6"),
				List.of(new ExpectedCharacteristic("Entity", "PrivateTaxi")),
				Map.of("brzxei",
						List.of(new ExpectedCharacteristic("CriticalDataType", "ContactInformation")),
						"l9ktzp",
						List.of(new ExpectedCharacteristic("CriticalDataType", "RouteDataType")))));
     */
    
    
   
    @Override
    public String toString() {
        return this.getModelName();
    }
    
    @Override
    public String getFileName() {
    	return "diagram";
    }
}