package org.palladiosimulator.dataflow.confidentiality.analysis.constraint;

public class CharacteristicValueData {
	private String characteristicType;
	private String characteristicValue;
	
	public CharacteristicValueData(String characteristicType, String characteristicValue) {
		this.characteristicType = characteristicType;
		this.characteristicValue = characteristicValue;
	}
	
	public String getCharacteristicType() {
		return characteristicType;
	}
	
	public String getCharacteristicValue() {
		return characteristicValue;
	}
}
