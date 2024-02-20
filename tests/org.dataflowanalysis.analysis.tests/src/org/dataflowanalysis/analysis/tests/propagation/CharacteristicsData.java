package org.dataflowanalysis.analysis.tests.propagation;

public class CharacteristicsData {
    private final int sequenceIndex;
    private final int elementIndex;
    private final String variable;
    private final String characteristicType;
    private final String characteristicValue;

    public CharacteristicsData(int sequenceIndex, int elementIndex, String variable, String characteristicType, String characteristicValue) {
        this.sequenceIndex = sequenceIndex;
        this.elementIndex = elementIndex;
        this.variable = variable;
        this.characteristicType = characteristicType;
        this.characteristicValue = characteristicValue;
    }

    public int getSequenceIndex() {
        return sequenceIndex;
    }

    public int getElementIndex() {
        return elementIndex;
    }

    public String getVariable() {
        return variable;
    }

    public String getCharacteristicType() {
        return characteristicType;
    }

    public String getCharacteristicValue() {
        return characteristicValue;
    }
}
