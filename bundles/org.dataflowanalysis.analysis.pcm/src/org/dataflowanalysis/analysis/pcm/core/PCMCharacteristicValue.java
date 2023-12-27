package org.dataflowanalysis.analysis.pcm.core;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;

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