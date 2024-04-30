package org.dataflowanalysis.analysis.dsl.selectors;


import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.List;

public class DataCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData dataCharacteristic;
    private final boolean inverted;

    public DataCharacteristicsSelector(CharacteristicsSelectorData dataCharacteristic) {
        this.dataCharacteristic = dataCharacteristic;
        this.inverted = false;
    }

    public DataCharacteristicsSelector(CharacteristicsSelectorData dataCharacteristic, boolean inverted) {
        this.dataCharacteristic = dataCharacteristic;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataCharacteristics().stream()
                .flatMap(it -> it.characteristics().stream())
                .toList();
        return this.inverted ?
                presentCharacteristics.stream().noneMatch(this.dataCharacteristic::matchesCharacteristic) :
                presentCharacteristics.stream().anyMatch(this.dataCharacteristic::matchesCharacteristic);
    }
}
