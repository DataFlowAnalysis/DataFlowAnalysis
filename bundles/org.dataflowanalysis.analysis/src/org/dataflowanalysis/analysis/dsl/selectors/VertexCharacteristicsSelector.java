package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexCharacteristicsSelector extends VertexSelector {
    private static final Logger logger = Logger.getLogger(VertexCharacteristicsSelector.class);

    private final CharacteristicsSelectorData vertexCharacteristics;
    private final boolean inverted;
    private final boolean recursive;

    public CharacteristicsSelectorData getCharacteristicsSelectorData() { return vertexCharacteristics; }

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
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        List<Boolean> results = new ArrayList<>();
        if (variableNames.isEmpty()) {
            List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = presentCharacteristics.stream()
                    .map(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it, ConstraintVariable.CONSTANT_NAME,
                            characteristicTypes, characteristicValues))
                    .toList();
            results.add(this.inverted ? matches.stream()
                    .noneMatch(it -> it)
                    : matches.stream()
                            .anyMatch(it -> it));
            boolean result = this.inverted ? results.stream()
                    .allMatch(it -> it)
                    : results.stream()
                            .anyMatch(it -> it);
            if (this.recursive) {
                return result || vertex.getPreviousElements()
                        .stream()
                        .anyMatch(this::matches);
            }
            return result;
        }
        for (String variableName : variableNames) {
            List<CharacteristicValue> presentCharacteristics = vertex.getAllVertexCharacteristics();
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = presentCharacteristics.stream()
                    .map(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it, variableName, characteristicTypes,
                            characteristicValues))
                    .toList();
            this.vertexCharacteristics.applyResults(context, vertex, variableName, characteristicTypes, characteristicValues);
            results.add(this.inverted ? matches.stream()
                    .noneMatch(it -> it)
                    : matches.stream()
                            .anyMatch(it -> it));
        }
        boolean result = this.inverted ? results.stream()
                .allMatch(it -> it)
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
        if (this.inverted) {
            return DSL_INVERTED_SYMBOL + this.vertexCharacteristics.toString();
        } else {
            return this.vertexCharacteristics.toString();
        }
    }

    /**
     * Parses a {@link VertexCharacteristicsSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code vertex <Type>.<Value>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexCharacteristicsSelector} object
     */
    public static ParseResult<VertexCharacteristicsSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse vertex characteristic selector from empty or invalid string!");
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
        return ParseResult.ok(new VertexCharacteristicsSelector(context, selectorData.getResult(), inverted));
    }

    public boolean isInverted() {
        return inverted;
    }
}
