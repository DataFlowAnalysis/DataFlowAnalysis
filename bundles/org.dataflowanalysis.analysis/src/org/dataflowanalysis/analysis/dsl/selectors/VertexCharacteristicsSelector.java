package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;

import java.util.List;

public class VertexCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData vertexCharacteristics;
    private final boolean inverted;

    public VertexCharacteristicsSelector(CharacteristicsSelectorData vertexCharacteristics) {
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = false;
    }

    public VertexCharacteristicsSelector(CharacteristicsSelectorData vertexCharacteristics, boolean inverted) {
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
        return this.inverted ?
                presentCharacteristics.stream().noneMatch(this.vertexCharacteristics::matchesCharacteristic) :
                presentCharacteristics.stream().anyMatch(this.vertexCharacteristics::matchesCharacteristic);
    }
}
