package org.dataflowanalysis.examplemodels.results;

public record ExpectedCharacteristic(String characteristicType, String characteristicLiteral) {

    public static ExpectedCharacteristic of(String characteristicType, String characteristicLiteral) {
        return new ExpectedCharacteristic(characteristicType, characteristicLiteral);
    }

    @Override
    public String toString() {
        return String.format("%s.%s", characteristicType, characteristicLiteral);
    }
}
