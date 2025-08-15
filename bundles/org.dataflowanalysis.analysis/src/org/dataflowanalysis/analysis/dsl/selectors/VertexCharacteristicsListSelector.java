package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexCharacteristicsListSelector extends VertexSelector {
    private static final Logger logger = Logger.getLogger(VertexCharacteristicsListSelector.class);

    private final List<CharacteristicsSelectorData> vertexCharacteristics;
    private final boolean inverted;
    private final boolean recursive;

    public List<CharacteristicsSelectorData> getCharacteristicsSelectorDataList() { return vertexCharacteristics; }

    public VertexCharacteristicsListSelector(DSLContext context, List<CharacteristicsSelectorData> vertexCharacteristics) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = false;
        this.recursive = false;
    }

    public VertexCharacteristicsListSelector(DSLContext context, List<CharacteristicsSelectorData> vertexCharacteristics, boolean inverted) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = false;
    }

    // TODO does this ever get used? call hierarchy shows no calls
    public VertexCharacteristicsListSelector(DSLContext context, List<CharacteristicsSelectorData> vertexCharacteristics, boolean inverted,
            boolean recursive) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = recursive;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        List<Boolean> results = new ArrayList<>();
        if (variableNames.isEmpty()) {
            for (CharacteristicsSelectorData vertexCharacteristic : this.vertexCharacteristics) {
                List<Boolean> vertexCharacteristicResult = new ArrayList<>();
                List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
                List<String> characteristicTypes = new ArrayList<>();
                List<String> characteristicValues = new ArrayList<>();
                List<Boolean> matches = presentCharacteristics.stream()
                        .map(it -> vertexCharacteristic.matchesCharacteristic(context, vertex, it, ConstraintVariable.CONSTANT_NAME,
                                characteristicTypes, characteristicValues))
                        .toList();
                vertexCharacteristicResult.add(this.inverted ? matches.stream()
                        .noneMatch(it -> it)
                        : matches.stream()
                                .anyMatch(it -> it));
                results.add(vertexCharacteristicResult.stream()
                        .anyMatch(it -> it));
            }

            boolean result = this.inverted ? results.stream()
                    .noneMatch(it -> it)
                    : results.stream()
                            .anyMatch(it -> it);
            if (this.recursive) {
                return result || vertex.getPreviousElements()
                        .stream()
                        .anyMatch(this::matches);
            }
            return result;
        }
        for (CharacteristicsSelectorData vertexCharacteristic : this.vertexCharacteristics) {
            List<Boolean> vertexCharacteristicResult = new ArrayList<>();
            for (String variableName : variableNames) {
                List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
                List<String> characteristicTypes = new ArrayList<>();
                List<String> characteristicValues = new ArrayList<>();
                List<Boolean> matches = presentCharacteristics.stream()
                        .map(it -> vertexCharacteristic.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes,
                                characteristicValues))
                        .toList();
                vertexCharacteristic.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);
                vertexCharacteristicResult.add(this.inverted ? matches.stream()
                        .noneMatch(it -> it)
                        : matches.stream()
                                .anyMatch(it -> it));
            }
            results.add(vertexCharacteristicResult.stream()
                    .anyMatch(it -> it));
        }

        boolean result = this.inverted ? results.stream()
                .noneMatch(it -> it)
                : results.stream()
                        .anyMatch(it -> it);
        if (this.recursive) {
            return result || vertex.getPreviousElements()
                    .stream()
                    .anyMatch(this::matches);
        }
        return result;
    }

    @Override
    public String toString() {
        StringJoiner vertexCharacteristicsString = new StringJoiner(DSL_DELIMITER);
        this.vertexCharacteristics.forEach(it -> vertexCharacteristicsString.add(it.toString()));
        if (this.inverted) {
            return DSL_INVERTED_SYMBOL + vertexCharacteristicsString;
        } else {
            return vertexCharacteristicsString.toString();
        }
    }

    /**
     * Parses a {@link VertexCharacteristicsSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code vertex <Type>.<Value>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexCharacteristicsSelector} object
     */
    public static ParseResult<VertexCharacteristicsListSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot vertex characteristic list selector from empty or invalid string!");
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
        return ParseResult.ok(new VertexCharacteristicsListSelector(context, selectors, inverted));
    }

    public boolean isInverted() {
        return inverted;
    }
}
