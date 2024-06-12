package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.DSLContext;

import java.util.List;

public class DataCharacteristicListSelector extends DataSelector {
    private final List<CharacteristicsSelectorData> dataCharacteristics;
    private final boolean inverted;

    public DataCharacteristicListSelector(DSLContext context, List<CharacteristicsSelectorData> dataCharacteristics) {
        super(context);
        this.dataCharacteristics = dataCharacteristics;
        this.inverted = false;
    }

    public DataCharacteristicListSelector(DSLContext context, List<CharacteristicsSelectorData> dataCharacteristics, boolean inverted) {
        super(context);
        this.dataCharacteristics = dataCharacteristics;
        this.inverted = inverted;
    }

    // TODO: Intersection does not work with multiple characteristics (as they are checked as single entities)
    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataCharacteristics().stream()
                .flatMap(it -> it.characteristics().stream())
                .toList();
        return this.inverted ?
                presentCharacteristics.stream().map(it -> this.dataCharacteristics.stream().anyMatch(dc -> dc.matchesCharacteristic(context, vertex, it))).noneMatch(it -> it) :
                presentCharacteristics.stream().map(it -> this.dataCharacteristics.stream().anyMatch(dc -> dc.matchesCharacteristic(context, vertex, it))).anyMatch(it -> it);
    }
}
