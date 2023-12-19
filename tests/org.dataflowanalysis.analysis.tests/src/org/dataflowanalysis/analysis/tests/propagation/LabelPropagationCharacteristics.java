package org.dataflowanalysis.analysis.tests.propagation;

import java.util.List;

public class LabelPropagationCharacteristics {
	
	public static final List<CharacteristicsData> travelPlannerCharacteristics = 
			List.of(new CharacteristicsData(0, 2, "ccd" , "GrantedRoles", "User"),
					new CharacteristicsData(0, 6, "query" , "GrantedRoles", "User"),
					new CharacteristicsData(0, 6, "query" , "GrantedRoles", "Airline"),
					new CharacteristicsData(1, 2, "flight" , "GrantedRoles", "User"),
					new CharacteristicsData(1, 2, "flight" , "GrantedRoles", "Airline"));
	
	public static final List<CharacteristicsData> onlineShopCharacteristics =
			List.of(new CharacteristicsData(0, 6, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 7, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 10, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 12, "userData", "DataSensitivity", "Personal"),
					new CharacteristicsData(1, 14, "userData", "DataSensitivity", "Personal"));
	
	public static final List<CharacteristicsData> internationalOnlineShopCharacteristics =
			List.of(new CharacteristicsData(0, 7, "RETURN", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 9, "inventory", "DataSensitivity", "Public"),
					new CharacteristicsData(0, 12, "userData", "DataSensitivity", "Personal"),
					new CharacteristicsData(0, 18, "userData", "DataSensitivity", "Personal"));
	
}
