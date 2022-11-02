package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.List;

public class LabelPropagationCharacteristics {
	
	public static final List<CharacteristicsData> travelPlannerCharacteristics = 
			List.of(new CharacteristicsData(0, 0, "ccd" , "GrantedRoles", "User"),
					new CharacteristicsData(0, 2, "query" , "GrantedRoles", "User"),
					new CharacteristicsData(0, 2, "query" , "GrantedRoles", "Airline"),
					new CharacteristicsData(1, 0, "flight" , "GrantedRoles", "User"),
					new CharacteristicsData(1, 0, "flight" , "GrantedRoles", "Airline"));
	
	public static final List<CharacteristicsData> onlineShopCharacteristics =
			List.of(new CharacteristicsData(0, 2, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 4, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 5, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 7, "userData", "DataSensitivity", "Personal"),
					new CharacteristicsData(1, 8, "userData", "DataSensitivity", "Personal"));
	
	public static final List<CharacteristicsData> internationalOnlineShopCharacteristics =
			List.of(new CharacteristicsData(0, 2, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 5, "inventory", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 6, "userData", "DataSensitivity", "Personal"),
					new CharacteristicsData(0, 9, "userData", "DataSensitivity", "Personal"));
	
}
