package org.dataflowanalysis.analysis.dsl.selectors;


import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.util.ArrayList;
import java.util.List;

public class DataCharacteristicsSelector extends DataSelector {
    private static final Logger logger = Logger.getLogger(DataCharacteristicsSelector.class);

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

    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        if (this.inverted) {
            return DSL_INVERTED_SYMBOL + dataCharacteristic.toString();
        } else {
            return dataCharacteristic.toString();
        }
    }

    public static ParseResult<DataCharacteristicsSelector> fromString(StringView string, DSLContext context) {
        logger.info("Parsing: " + string.getString());
        boolean inverted = string.getString().startsWith(DSL_INVERTED_SYMBOL);
        if (inverted) string.advance(DSL_INVERTED_SYMBOL.length());
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        if (selectorData.failed()) {
            if (inverted) string.retreat(DSL_INVERTED_SYMBOL.length());
            return ParseResult.error(selectorData.getError());
        }
        string.advance(1);
        return ParseResult.ok(new DataCharacteristicsSelector(context, selectorData.getResult(), inverted));
    }
}
