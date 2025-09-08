package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class ACDistanceTrackerViolationResult implements DFDExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "AC-DistanceTracker-violation";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	return List.of(new ConstraintDSL().ofData()
    			.withoutLabel("AccessRights", "DistanceTracker")
    			.neverFlows()
    			.toVertex()
    			.withCharacteristic("Roles", "DistanceTracker")
    			.create());
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
    	return List.of(
        		new ExpectedViolation(1, new DFDIdentifier("d6mr08"),
        				List.of(new ExpectedCharacteristic("Roles", "DistanceTracker")),
        				Map.of("466m7",
        						List.of(new ExpectedCharacteristic("AccessRights", "User"), new ExpectedCharacteristic("AccessRights", "TrackingService")))),
        		new ExpectedViolation(1, new DFDIdentifier("eq0c5k"),
        				List.of(new ExpectedCharacteristic("Roles", "DistanceTracker")),
        				Map.of("7r5i93",
        						List.of(new ExpectedCharacteristic("AccessRights", "User"), new ExpectedCharacteristic("AccessRights", "TrackingService")))));	
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