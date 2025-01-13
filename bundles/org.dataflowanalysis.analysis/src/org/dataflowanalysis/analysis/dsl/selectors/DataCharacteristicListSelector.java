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
import java.util.StringJoiner;

public class DataCharacteristicListSelector extends DataSelector {
    private static final Logger logger = Logger.getLogger(DataCharacteristicListSelector.class);
    private static final String DSL_DELIMITER = ",";

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

    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        StringJoiner dataCharacteristicsString = new StringJoiner(DSL_DELIMITER);
        this.dataCharacteristics.forEach(it -> dataCharacteristicsString.add(it.toString()));
        if (this.inverted) {
            return "!" + dataCharacteristicsString;
        } else {
            return dataCharacteristicsString.toString();
        }
    }

    public static ParseResult<DataCharacteristicListSelector> fromString(StringView string, DSLContext context) {
        logger.info("Parsing: " + string.getString());
        boolean inverted = string.getString().startsWith("!");
        if (inverted) string.advance(1);
        List<CharacteristicsSelectorData> selectors = new ArrayList<>();
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        while (selectorData.successful() && !(string.startsWith(" ") || string.getString().isEmpty())) {
            selectors.add(selectorData.getResult());
            if (!string.startsWith(DSL_DELIMITER)) {
                string.retreat(1);
                return string.expect(DSL_DELIMITER);
            }
            string.advance(DSL_DELIMITER.length());
            selectorData = CharacteristicsSelectorData.fromString(string);
        }
        if (selectorData.failed()) {
            string.retreat(1);
            return ParseResult.error(selectorData.getError());
        }
        if (selectors.isEmpty()) {
            string.retreat(1);
            return ParseResult.error("Cannot parse data characteristic list selector as the list is empty!");
        }
        string.advance(1);
        return ParseResult.ok(new DataCharacteristicListSelector(context, selectors, inverted));
    }
}
