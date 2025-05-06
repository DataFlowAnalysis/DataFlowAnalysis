package org.dataflowanalysis.analysis.pcm.core;

import java.util.Objects;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.EnumCharacteristicType;
import org.dataflowanalysis.pcm.extension.dictionary.characterized.DataDictionaryCharacterized.Literal;

public final class PCMCharacteristicValue implements CharacteristicValue {
    private final EnumCharacteristicType characteristicType;
    private final Literal characteristicLiteral;

    public PCMCharacteristicValue(EnumCharacteristicType characteristicType, Literal characteristicLiteral) {
        if (Objects.isNull(characteristicType) || Objects.isNull(characteristicType.getName()) || characteristicType.getName()
                .isBlank()) {
            throw new IllegalArgumentException("Characteristic type cannot be null or empty");
        }
        if (Objects.isNull(characteristicLiteral) || Objects.isNull(characteristicLiteral.getName()) || characteristicLiteral.getName()
                .isBlank()) {
            throw new IllegalArgumentException("Characteristic literal cannot be null or empty");
        }
        this.characteristicType = characteristicType;
        this.characteristicLiteral = characteristicLiteral;
    }

    @Override
    public String getTypeName() {
        return this.characteristicType()
                .getName();
    }

    @Override
    public String getValueName() {
        return this.characteristicLiteral()
                .getName();
    }

    @Override
    public String getValueId() {
        return this.characteristicLiteral()
                .getId();
    }

    @Override
    public String toString() {
        return String.format("%s.%s", this.getTypeName(), this.getValueName());
    }

    public EnumCharacteristicType characteristicType() {
        return characteristicType;
    }

    public Literal characteristicLiteral() {
        return characteristicLiteral;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (PCMCharacteristicValue) obj;
        return Objects.equals(this.characteristicType, that.characteristicType)
                && Objects.equals(this.characteristicLiteral, that.characteristicLiteral);
    }

    @Override
    public int hashCode() {
        return Objects.hash(characteristicType, characteristicLiteral);
    }

}
