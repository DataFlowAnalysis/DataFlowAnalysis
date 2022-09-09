package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public record CharacteristicValue(EnumCharacteristicType characteristicType, Literal characteristicLiteral) {

}
