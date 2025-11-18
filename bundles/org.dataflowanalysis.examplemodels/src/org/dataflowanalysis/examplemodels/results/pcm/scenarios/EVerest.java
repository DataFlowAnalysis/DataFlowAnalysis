package org.dataflowanalysis.examplemodels.results.pcm.scenarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.examplemodels.results.ExpectedCharacteristic;
import org.dataflowanalysis.examplemodels.results.ExpectedViolation;
import org.dataflowanalysis.examplemodels.results.pcm.PCMExampleModelResult;
import org.dataflowanalysis.examplemodels.results.pcm.PCMIdentifier;

public class EVerest implements PCMExampleModelResult {
    @Override
    public String getBaseFolderName() {
        return "scenarios";
    }

    @Override
    public String getModelName() {
        return "EVerest";
    }

    @Override
    public List<AnalysisConstraint> getDSLConstraints() {
    	List<AnalysisConstraint> constraints = new ArrayList<>();
        constraints.add(new ConstraintDSL()
				.ofData()
				.withoutLabel("ActorStatus", List.of("Authorized"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Component", List.of("API"))
				.create());
        
        return constraints;
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(new ExpectedViolation(6, PCMIdentifier.of("_nDVhIT7IEe-HHLw-F3Rezw"),
                List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_9qmVwD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_9qmVwD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_nDVhIj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(7, PCMIdentifier.of("_nDcO0T7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(7, PCMIdentifier.of("_E1-XUD91Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(7, PCMIdentifier.of("_E1-XUD91Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(7, PCMIdentifier.of("_nDcO0j7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_nChBwT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_z8B0ID9zEe-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_z8B0ID9zEe-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")), "value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status", "Anonymized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_nChBwj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")), "value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status", "Anonymized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_nCvrQT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_bv2HgD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_bv2HgD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")), "value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status", "Anonymized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_nCvrQj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")), "value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status", "Anonymized")))),
        		new ExpectedViolation(12, PCMIdentifier.of("_nDOzcT7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(12, PCMIdentifier.of("_vVDzkD90Ee-T3I6VeO-56g", true),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(12, PCMIdentifier.of("_vVDzkD90Ee-T3I6VeO-56g", false),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(12, PCMIdentifier.of("_nDOzcj7IEe-HHLw-F3Rezw"),
                        List.of(new ExpectedCharacteristic("Component", "API"),new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"))))
        		
        		);
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
