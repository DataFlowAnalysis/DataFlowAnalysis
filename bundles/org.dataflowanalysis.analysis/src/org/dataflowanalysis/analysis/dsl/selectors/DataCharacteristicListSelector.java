package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

import java.util.ArrayList;
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
                    .map(it -> this.dataCharacteristics.stream().anyMatch(dc -> dc.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes, characteristicValues)))
                    .toList();
            this.dataCharacteristics.forEach(it -> it.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues));
            if(result) {
                result = this.inverted ?
                        matches.stream().noneMatch(it -> it) :
                        matches.stream().anyMatch(it -> it);
            }
        }
        return result;
    }
}
