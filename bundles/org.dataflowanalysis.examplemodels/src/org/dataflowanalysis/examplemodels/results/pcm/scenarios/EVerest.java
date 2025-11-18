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
    	
    	//Requirement #6:
    	//Tokens used for authentication should not be stored in plain text in log files or persistent storage.
    	//Authentication tokens should not leak to the outside
    	constraints.add(new ConstraintDSL()
				.ofData()
				.withLabel("Status", List.of("Token"))
				.withoutLabel("Status", List.of("Hashed", "Encrypted"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Location", List.of("TPM", "LocalStorage", "External"))
				.create());
    	constraints.add(new ConstraintDSL()
				.ofData()
				.withLabel("Status", List.of("Token"))
				.withoutLabel("Status", List.of("Hashed", "Encrypted"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Location", List.of("Logs"))
				.create());
    	
    	//Requirement #25:
    	//The OCPP modules shall store only the hash of the authorization token of an EV user persistently
    	constraints.add(new ConstraintDSL()
				.ofData()
				.withLabel("Status", List.of("Token"))
				.withoutLabel("Status", List.of("Hashed"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Location", List.of("LocalStorage", "TPM"))
				.create());
    	
    	constraints.add(new ConstraintDSL()
				.ofData()
				.withLabel("Status", List.of("Token"))
				.withoutLabel("Status", List.of("Hashed"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Component", List.of("Logs"))
				.create());
    	
    	
    	
        constraints.add(new ConstraintDSL()
				.ofData()
				.withoutLabel("ActorStatus", List.of("Authorized"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Component", List.of("API"))
				.create()
				);
        constraints.add(new ConstraintDSL()
				.ofData()
				.withLabel("Status", List.of("Token"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Component", List.of("Logs"))
				.create());
        constraints.add(new ConstraintDSL()
				.ofData()
				.withoutLabel("ActorStatus", List.of("Authorized"))
				.fromNode()
				.neverFlows()
				.toVertex()
				.withCharacteristic("Location", List.of("ChargingStation"))
				.create());
        
        return constraints;
    }

    @Override
    public List<ExpectedViolation> getExpectedViolations() {
        return List.of(
        		new ExpectedViolation(1, PCMIdentifier.of("_zTBm4DOgEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_hM4rcDOhEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_fZbbMEODEe-0EeXsSysZ9A", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")), "reservation_result", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_JyylAigbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_JyzMECgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_fZbbMEODEe-0EeXsSysZ9A",false),
        				List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"reservation",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")), "reservation_result", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_DFF9EFUBEe-wM6yAw99YUA"),
        				List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"reservation",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")), "reservation_result", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(1, PCMIdentifier.of("_zTBm4TOgEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("RETURN", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")),"reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"reservation",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive")), "reservation_result", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Encrypted"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_xHsrAE0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_0oJ6AE0LEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_Nl18oDxjEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_xkBJQDxlEe-LH4VHtqs82Q",true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_xkBJQDxlEe-LH4VHtqs82Q",false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(2, PCMIdentifier.of("_xHsrAU0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(3, PCMIdentifier.of("_17NEkDxoEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("resetType", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(3, PCMIdentifier.of("_17NEkTxoEe-LH4VHtqs82Q"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("resetType", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_YLIkwEpjEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_06BDsEpmEe-QePQiuhxJfA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_WYPuAEpnEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_YM6JkEpnEe-QePQiuhxJfA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_K2QRMH8PEe-Ips9OktXuUA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("provided_token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_K2QRMX8PEe-Ips9OktXuUA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("provided_token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_YM6JkEpnEe-QePQiuhxJfA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("provided_token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_06BDsEpmEe-QePQiuhxJfA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_Z3mS0HBQEe-sCL8KfTMM7A", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "Logs")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_Z3mS0HBQEe-sCL8KfTMM7A", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_YLIkwUpjEe-QePQiuhxJfA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation"),new ExpectedCharacteristic("Component", "PN532TokenProvider")),
                        Map.of("logmessage", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"providedToken", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")),"token", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Token"),new ExpectedCharacteristic("Status","Sensitive")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSgVY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Component", "Logs"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("logmessage",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Token")))),
        		new ExpectedViolation(5, PCMIdentifier.of("_oCeSglY4Ee-lU5UkonmXoQ"),
                        List.of(new ExpectedCharacteristic("Component", "Logs"),new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("logmessage",List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Token")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_DL6XoigbEe-0jJudbTQUyQ"),
        				List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
        				Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_DL6-sCgbEe-0jJudbTQUyQ"),
        				List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
        				Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(6, PCMIdentifier.of("_nDVhIT7IEe-HHLw-F3Rezw"),
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
        		new ExpectedViolation(7, PCMIdentifier.of("_BlU0USgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(7, PCMIdentifier.of("_BlU0UCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
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
        		new ExpectedViolation(10, PCMIdentifier.of("_L3MFsCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_13ijACjUEe-vi6u5C9ZFsw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(10, PCMIdentifier.of("_L3MFsSgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("local_energy_limits", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")),"value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
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
        		new ExpectedViolation(11, PCMIdentifier.of("_L3MFsCgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_13ijACjUEe-vi6u5C9ZFsw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(11, PCMIdentifier.of("_L3MFsSgbEe-0jJudbTQUyQ"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("local_energy_limits", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")),"value", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"), new ExpectedCharacteristic("Status","Anonymized")))),
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
        		new ExpectedViolation(12, PCMIdentifier.of("_jwf4oSNDEe-9BLE8eIVxZg"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
        		new ExpectedViolation(12, PCMIdentifier.of("_QrtjoSNEEe-9BLE8eIVxZg"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("connector_id", List.of(new ExpectedCharacteristic("ActorStatus", "Authorized")))),
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
                        Map.of("data", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"connector_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_ddU6gEWqEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_mf_04EWqEe-vaMxYiHuBmw", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_s31PoDOhEe-thK3JRY5OhA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_2SADIEWsEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_Byo0wEWtEe-vaMxYiHuBmw",true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_Byo0wEWtEe-vaMxYiHuBmw",false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(13, PCMIdentifier.of("_LUBEYEWtEe-vaMxYiHuBmw"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("reservation_id", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"),new ExpectedCharacteristic("Status","Sensitive"),new ExpectedCharacteristic("Status","Anonymized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_zRGngE0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_AILVsE0OEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_92x60E0NEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_Uj7oME0OEe-j2Yo8i_9OIA", true),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_Uj7oME0OEe-j2Yo8i_9OIA", false),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")))),
        		new ExpectedViolation(14, PCMIdentifier.of("_zRGngU0LEe-j2Yo8i_9OIA"),
                        List.of(new ExpectedCharacteristic("Location", "ChargingStation")),
                        Map.of("msg", List.of(new ExpectedCharacteristic("ActorStatus","Authorized")),"request", List.of(new ExpectedCharacteristic("ActorStatus","Authorized"))))
        		
        		);
    }

    @Override
    public String toString() {
        return this.getModelName();
    }
}
