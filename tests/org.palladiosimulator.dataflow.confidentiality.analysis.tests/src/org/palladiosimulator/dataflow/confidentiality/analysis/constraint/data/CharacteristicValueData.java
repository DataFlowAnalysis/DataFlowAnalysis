package org.palladiosimulator.dataflow.confidentiality.analysis.constraint.data;

public record CharacteristicValueData(String characteristicType, String characteristicValue) {
	public CharacteristicValueData(String characteristicType, String characteristicValue) {
		this.characteristicType = characteristicType;
		this.characteristicValue = characteristicValue;
	}
}
