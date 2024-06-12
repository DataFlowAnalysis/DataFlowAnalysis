package org.dataflowanalysis.analysis.dsl.selectors;


import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.DSLContext;

import java.util.List;

public class DataCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData dataCharacteristic;
    private final boolean inverted;

    public DataCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData dataCharacteristic) {
        super(context);
        this.dataCharacteristic = dataCharacteristic;
        this.inverted = false;
    }

    public DataCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData dataCharacteristic, boolean inverted) {
        super(context);
        this.dataCharacteristic = dataCharacteristic;
        this.inverted = inverted;
    }

    // TODO: Intersection does not work with multiple characteristics (as they are checked as single entities)
    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataCharacteristics().stream()
                .flatMap(it -> it.characteristics().stream())
                .toList();
        return this.inverted ?
                presentCharacteristics.stream().map(it -> this.dataCharacteristic.matchesCharacteristic(context, vertex, it)).noneMatch(it -> it) :
                presentCharacteristics.stream().map(it -> this.dataCharacteristic.matchesCharacteristic(context, vertex, it)).anyMatch(it -> it);
    }
}
