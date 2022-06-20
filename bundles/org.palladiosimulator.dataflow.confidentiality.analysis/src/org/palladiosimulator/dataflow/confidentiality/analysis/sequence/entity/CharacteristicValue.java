package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public class CharacteristicValue {
    private final EnumCharacteristicType characteristicType;
    private final Literal characteristicLiteral;

    public CharacteristicValue(EnumCharacteristicType characteristicType, Literal characteristicLiteral) {
        this.characteristicType = characteristicType;
        this.characteristicLiteral = characteristicLiteral;
    }

    public EnumCharacteristicType getCharacteristicType() {
        return characteristicType;
    }

    public Literal getCharacteristicLiteral() {
        return characteristicLiteral;
    }
}
