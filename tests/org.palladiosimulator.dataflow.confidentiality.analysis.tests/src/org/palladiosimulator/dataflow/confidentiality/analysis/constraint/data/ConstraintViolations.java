package org.palladiosimulator.dataflow.confidentiality.analysis.constraint.data;

import java.util.List;
import java.util.Map;

public class ConstraintViolations {
	public static final List<ConstraintData> travelPlannerViolations = List.of(
			new ConstraintData("_vorK8fVeEeuMKba1Qn68bg", 
					List.of(new CharacteristicValueData("AssignedRoles", "Airline")), 
					Map.of(
						"flight", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline")),
						"ccd", List.of(new CharacteristicValueData("GrantedRoles", "User"))
					)),
				
				new ConstraintData("_7HCu4PViEeuMKba1Qn68bg",
						List.of(new CharacteristicValueData("AssignedRoles", "Airline")), 
						Map.of(
							"flight", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline")),
							"ccd", List.of(new CharacteristicValueData("GrantedRoles", "User")),
							"RETURN", List.of(new CharacteristicValueData("GrantedRoles", "User"), new CharacteristicValueData("GrantedRoles", "Airline"))
						))
			);
	
	public static final List<ConstraintData> internationalOnlineShopViolations = List.of(
			new ConstraintData("_oGmXgYTjEeywmO_IpTxeAg", 
					List.of(new CharacteristicValueData("ServerLocation", "nonEU")), 
					Map.of(
						"userData", List.of(new CharacteristicValueData("DataSensitivity", "Personal"))
					))
			);
	
	public static final List<ConstraintData> multipleRessourcesViolations = List.of(
			new ConstraintData("_dQ568HQSEe2fd909RlIZZw", 
					List.of(new CharacteristicValueData("ServerLocation", "nonEU"), new CharacteristicValueData("ServerLocation", "EU")),
					Map.of(
						"userdata", List.of(new CharacteristicValueData("DataSensitivity", "Personal"))
					))
			);
	
	public static final List<ConstraintData> dataStoreViolations = List.of(
			new ConstraintData("_elixoHQdEe2W39w_cTGxjg", 
					List.of(new CharacteristicValueData("AssignedRole", "User")), 
					Map.of(
						"RETURN", List.of(new CharacteristicValueData("GrantedRole", "Admin")),
						"ccd", List.of(new CharacteristicValueData("GrantedRole", "Admin"))
					))
			);
	
	public static final List<ConstraintData> returnViolations = List.of(
			new ConstraintData("_nOhAgILtEe2YyoqaKVkqog",
					List.of(new CharacteristicValueData("AssignedRole", "User")),
					Map.of(
						"RETURN", List.of(new CharacteristicValueData("AssignedRole", "Admin")),
						"data", List.of(new CharacteristicValueData("AssignedRole", "Admin"))
					))
			);
	
	private ConstraintViolations() {
		// Utility class
	}
}
