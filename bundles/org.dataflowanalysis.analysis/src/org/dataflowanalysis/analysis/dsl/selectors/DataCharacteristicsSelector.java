package org.dataflowanalysis.analysis.dsl.selectors;


import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.List;

public class DataCharacteristicsSelector extends DataSelector {
    private final List<CharacteristicsSelectorData> dataCharacteristics;
    private final boolean inverted;

    public DataCharacteristicsSelector(List<CharacteristicsSelectorData> dataCharacteristics) {
        this.dataCharacteristics = dataCharacteristics;
        this.inverted = false;
    }

    public DataCharacteristicsSelector(List<CharacteristicsSelectorData> dataCharacteristics, boolean inverted) {
        this.dataCharacteristics = dataCharacteristics;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataFlowVariables().stream()
                .flatMap(it -> it.characteristics().stream())
                .toList();
        if (this.inverted) {
            return presentCharacteristics.stream()
                    .noneMatch(it -> this.dataCharacteristics.stream()
                            .anyMatch(characteristic -> characteristic.matchesCharacteristic(it)));
        } else {
            return presentCharacteristics.stream()
                    .anyMatch(it -> this.dataCharacteristics.stream()
                            .anyMatch(characteristic -> characteristic.matchesCharacteristic(it)));
        }
    }
}
