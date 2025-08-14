package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class DataCharacteristicsSelector extends DataSelector {
    private static final Logger logger = Logger.getLogger(DataCharacteristicsSelector.class);

    private final CharacteristicsSelectorData dataCharacteristic;
    private final boolean inverted;
    
    public CharacteristicsSelectorData getDataCharacteristic() { return dataCharacteristic; }

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
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        if (variableNames.isEmpty()) {
            return false;
        }
        List<Boolean> results = new ArrayList<>();
        for (String variableName : variableNames) {
            List<CharacteristicValue> presentCharacteristics = vertex.getAllIncomingDataCharacteristics()
                    .stream()
                    .filter(it -> it.variableName()
                    		.equals(variableName))
                    .flatMap(it -> it.characteristics()
                            .stream())
                    .toList();
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = presentCharacteristics.stream()
                    .map(it -> this.dataCharacteristic.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes,
                            characteristicValues))
                    .toList();
            this.dataCharacteristic.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);
            results.add(this.inverted ? matches.stream()
                    .noneMatch(it -> it)
                    : matches.stream()
                            .anyMatch(it -> it));
        }
        return this.inverted ? results.stream()
                .allMatch(it -> it)
                : results.stream()
                        .anyMatch(it -> it);
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

    /**
     * Parses a {@link DataCharacteristicsSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code [!]<Type>.<Value>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link DataCharacteristicsSelector} object
     */
    public static ParseResult<DataCharacteristicsSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse data characteristic selector from empty or invalid string!");
        }
        logger.info("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = string.getString()
                .startsWith(DSL_INVERTED_SYMBOL);
        if (inverted)
            string.advance(DSL_INVERTED_SYMBOL.length());
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        if (selectorData.failed()) {
            string.setPosition(position);
            return ParseResult.error(selectorData.getError());
        }
        string.advance(1);
        return ParseResult.ok(new DataCharacteristicsSelector(context, selectorData.getResult(), inverted));
    }
}
