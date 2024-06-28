package org.dataflowanalysis.analysis.dsl.selectors;


import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

import java.util.ArrayList;
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

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics().stream()
                .map(DataCharacteristic::variableName)
                .toList();
        if(variableNames.isEmpty()) {
        	return false;
        }
        boolean result = true;
        for(String variableName : variableNames) {
            List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataCharacteristics().stream()
                    .filter(it -> it.variableName().equals(variableName))
                    .flatMap(it -> it.characteristics().stream())
                    .toList();
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = presentCharacteristics.stream()
                    .map(it -> this.dataCharacteristic.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes, characteristicValues))
                    .toList();
            this.dataCharacteristic.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);

            if(result) {
                result = this.inverted ?
                        matches.stream().noneMatch(it -> it) :
                        matches.stream().anyMatch(it -> it);
            }
        }
        return result;
    }
}
