package org.dataflowanalysis.analysis.characteristics;

import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;

public record CharacteristicValue(EnumCharacteristicType characteristicType, Literal characteristicLiteral) {

}
