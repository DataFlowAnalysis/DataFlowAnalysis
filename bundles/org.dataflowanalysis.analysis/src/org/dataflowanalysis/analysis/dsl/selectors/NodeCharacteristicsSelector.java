package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.List;

public class NodeCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData nodeCharacteristic;
    private final boolean inverted;

    public NodeCharacteristicsSelector(CharacteristicsSelectorData nodeCharacteristic) {
        this.nodeCharacteristic = nodeCharacteristic;
        this.inverted = false;
    }

    public NodeCharacteristicsSelector(CharacteristicsSelectorData nodeCharacteristic, boolean inverted) {
        this.nodeCharacteristic = nodeCharacteristic;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllNodeCharacteristics();
        return this.inverted ?
                presentCharacteristics.stream().noneMatch(this.nodeCharacteristic::matchesCharacteristic) :
                presentCharacteristics.stream().anyMatch(this.nodeCharacteristic::matchesCharacteristic);
    }
}
