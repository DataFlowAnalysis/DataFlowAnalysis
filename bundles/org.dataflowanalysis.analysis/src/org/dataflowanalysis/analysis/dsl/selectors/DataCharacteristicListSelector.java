package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class DataCharacteristicListSelector extends DataSelector {
    private static final Logger logger = Logger.getLogger(DataCharacteristicListSelector.class);

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
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        if (variableNames.isEmpty()) {
            return false;
        }
        List<Boolean> results = new ArrayList<>();
        for (CharacteristicsSelectorData dataCharacteristic : this.dataCharacteristics) {
            List<Boolean> dataCharacteristicResult = new ArrayList<>();
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
                        .map(it -> dataCharacteristic.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes,
                                characteristicValues))
                        .toList();
                dataCharacteristic.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);
                dataCharacteristicResult.add(this.inverted ? matches.stream()
                        .noneMatch(it -> it)
                        : matches.stream()
                                .anyMatch(it -> it));
            }
            results.add(dataCharacteristicResult.stream()
                    .anyMatch(it -> it));
        }

        return this.inverted ? results.stream()
                .noneMatch(it -> it)
                : results.stream()
                        .anyMatch(it -> it);
    }

    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        StringJoiner dataCharacteristicsString = new StringJoiner(DSL_DELIMITER);
        this.dataCharacteristics.forEach(it -> dataCharacteristicsString.add(it.toString()));
        if (this.inverted) {
            return DSL_INVERTED_SYMBOL + dataCharacteristicsString;
        } else {
            return dataCharacteristicsString.toString();
        }
    }

    /**
     * Parses a {@link DataCharacteristicListSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code ([!]<Type>.<Value> )*}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link DataCharacteristicListSelector} object
     */
    public static ParseResult<DataCharacteristicListSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse characteristic list selector from empty or invalid string!");
        }
        logger.info("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = string.getString()
                .startsWith(DSL_INVERTED_SYMBOL);
        if (inverted)
            string.advance(DSL_INVERTED_SYMBOL.length());
        List<CharacteristicsSelectorData> selectors = new ArrayList<>();
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        if (selectorData.successful()) {
            selectors.add(selectorData.getResult());
        }
        while (!(string.empty() || string.invalid() || string.startsWith(" "))) {
            if (!string.startsWith(DSL_DELIMITER)) {
                string.setPosition(position);
                return string.expect(DSL_DELIMITER);
            }
            string.advance(DSL_DELIMITER.length());
            selectorData = CharacteristicsSelectorData.fromString(string);
            if (selectorData.successful()) {
                selectors.add(selectorData.getResult());
            } else {
                break;
            }
        }
        if (selectorData.failed()) {
            string.setPosition(position);
            return ParseResult.error(selectorData.getError());
        }
        if (selectors.size() <= 1) {
            string.setPosition(position);
            return ParseResult.error("Cannot parse data characteristic list selector as the list is empty or one element!");
        }
        string.advance(1);
        return ParseResult.ok(new DataCharacteristicListSelector(context, selectors, inverted));
    }
}
