package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.List;

public class NodeCharacteristicsSelector extends DataSelector {
    private final List<CharacteristicsSelectorData> nodeCharacteristics;
    private final boolean inverted;

    public NodeCharacteristicsSelector(List<CharacteristicsSelectorData> nodeCharacteristics) {
        this.nodeCharacteristics = nodeCharacteristics;
        this.inverted = false;
    }

    public NodeCharacteristicsSelector(List<CharacteristicsSelectorData> nodeCharacteristics, boolean inverted) {
        this.nodeCharacteristics = nodeCharacteristics;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllNodeCharacteristics();
        if (this.inverted) {
            return presentCharacteristics.stream()
                    .noneMatch(it -> this.nodeCharacteristics.stream()
                            .anyMatch(characteristic -> characteristic.matchesCharacteristic(it)));
        } else {
            return presentCharacteristics.stream()
                    .anyMatch(it -> this.nodeCharacteristics.stream()
                            .anyMatch(characteristic -> characteristic.matchesCharacteristic(it)));
        }
    }
}
