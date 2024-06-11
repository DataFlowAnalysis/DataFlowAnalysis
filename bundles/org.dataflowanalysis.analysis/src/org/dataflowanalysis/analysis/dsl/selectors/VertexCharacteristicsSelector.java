package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.DSLContext;

import java.util.List;

public class VertexCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData vertexCharacteristics;
    private final boolean inverted;

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = false;
    }

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics, boolean inverted) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
        return this.inverted ?
                presentCharacteristics.stream().noneMatch(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it)) :
                presentCharacteristics.stream().anyMatch(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it));
    }
}
