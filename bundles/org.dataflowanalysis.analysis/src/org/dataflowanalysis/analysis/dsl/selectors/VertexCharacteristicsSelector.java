package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

import java.util.ArrayList;
import java.util.List;

public class VertexCharacteristicsSelector extends DataSelector {
    private final CharacteristicsSelectorData vertexCharacteristics;
    private final boolean inverted;
    private final boolean recursive;

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = false;
        this.recursive = false;
    }

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics, boolean inverted) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = false;
    }

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics, boolean inverted, boolean recursive) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = recursive;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics().stream()
                .map(DataCharacteristic::variableName)
                .toList();
        boolean result = !variableNames.isEmpty();
        for (String variableName : variableNames) {
            List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = presentCharacteristics.stream().map(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes, characteristicValues)).toList();
            this.vertexCharacteristics.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);
            if (result) {
                result = this.inverted ?
                        matches.stream().noneMatch(it -> it) :
                        matches.stream().anyMatch(it -> it);
            }
        }
        if (this.recursive) {
            return result || vertex.getPreviousElements().stream().anyMatch(this::matches);
        }
        return result;
    }
}
