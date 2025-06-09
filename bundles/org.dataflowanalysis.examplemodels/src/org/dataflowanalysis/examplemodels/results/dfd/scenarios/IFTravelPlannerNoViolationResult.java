package org.dataflowanalysis.examplemodels.results.dfd.scenarios;

import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.dfd.DFDExampleModelResult;
import org.dataflowanalysis.examplemodels.results.dfd.DFDIdentifier;

public class IFTravelPlannerNoViolationResult implements DFDExampleModelResult {
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
        return null;
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}