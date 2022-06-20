package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayList;
import java.util.List;

public class DataFlowVariable {

    private final String variableName;
    private final List<CharacteristicValue> characteristics;

    public DataFlowVariable(String variableName) {
        this.variableName = variableName;
        this.characteristics = new ArrayList<>();
    }

    public String getVariableName() {
        return variableName;
    }

    public void addCharacteristic(CharacteristicValue characteristic) {
        this.characteristics.add(characteristic);
    }

    public List<CharacteristicValue> getAllCharacteristics() {
        return this.characteristics;
    }

}
