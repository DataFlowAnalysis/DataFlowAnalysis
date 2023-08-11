package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics;

import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public record PCMCharacteristicValue(EnumCharacteristicType characteristicType, Literal characteristicLiteral) implements CharacteristicValue{

	@Override
	public String getTypeName() {
		return this.characteristicType().getName();
	}

	@Override
	public String getValueName() {
		return this.characteristicLiteral().getName();
	}

	@Override
	public String getValueId() {
		return this.characteristicLiteral().getId();
	}

}
