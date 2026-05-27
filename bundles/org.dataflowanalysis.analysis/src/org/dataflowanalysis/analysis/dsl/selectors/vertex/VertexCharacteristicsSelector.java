package org.dataflowanalysis.analysis.dsl.selectors.vertex;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.CharacteristicsSelectorData;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexCharacteristicsSelector extends VertexSelector {
    private static final Logger logger = LoggerManager.getLogger(VertexCharacteristicsSelector.class);

    private final CharacteristicsSelectorData vertexCharacteristics;
    private final boolean inverted;
    private final boolean recursive;

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = false;
        this.recursive = false;
    }

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics,
            boolean inverted) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = false;
    }

    public VertexCharacteristicsSelector(DSLContext context, CharacteristicsSelectorData vertexCharacteristics,
            boolean inverted, boolean recursive) {
        super(context);
        this.vertexCharacteristics = vertexCharacteristics;
        this.inverted = inverted;
        this.recursive = recursive;
    }

    @Override
    public boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return this.matches(vertex, dslConstraintTrace, vertex.getPreviousVertexInformation()
                .getPreviousVertexCharacteristics()
                .stream()
                .toList());
    }

    @Override
    public boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace) {
        return this.matches(vertex, dslConstraintTrace, vertex.getAllVertexCharacteristics());
    }

    private boolean matches(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace,
            List<CharacteristicValue> vertexCharacteristic) {
        List<String> variableNames = vertex.getAllIncomingDataCharacteristics()
                .stream()
                .map(DataCharacteristic::variableName)
                .toList();
        List<Boolean> results = new ArrayList<>();
        if (variableNames.isEmpty()) {
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = vertexCharacteristic.stream()
                    .map(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it,
                            ConstraintVariable.CONSTANT_NAME, characteristicTypes, characteristicValues))
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
                        .anyMatch(it -> this.matches(it, dslConstraintTrace, vertexCharacteristic));
            }
            return result;
        }
        for (String variableName : variableNames) {
            List<String> characteristicTypes = new ArrayList<>();
            List<String> characteristicValues = new ArrayList<>();
            List<Boolean> matches = vertexCharacteristic.stream()
                    .map(it -> this.vertexCharacteristics.matchesCharacteristic(context, vertex, it, variableName,
                            characteristicTypes, characteristicValues))
                    .toList();
            this.vertexCharacteristics.applyResults(context, vertex, variableName, characteristicTypes,
                    characteristicValues);
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
                    .anyMatch(it -> this.matches(it, dslConstraintTrace, vertexCharacteristic));
        }
        return result;
    }

    @Override
    public String toString() {
        if (this.inverted) {
            return super.toString() + " " + AbstractParseable.DSL_INVERTED_SYMBOL
                    + this.vertexCharacteristics.toString();
        } else {
            return super.toString() + " " + this.vertexCharacteristics.toString();
        }
    }

    /**
     * Parses a {@link VertexCharacteristicsSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code vertex <Type>.<Value>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexCharacteristicsSelector} object
     */
    public static ParseResult<VertexSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse vertex characteristic selector from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = string.getString()
                .startsWith(AbstractParseable.DSL_INVERTED_SYMBOL);
        if (inverted)
            string.advance(AbstractParseable.DSL_INVERTED_SYMBOL.length());
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        if (selectorData.failed()) {
            string.setPosition(position);
            return ParseResult.error(selectorData.getError());
        }
        return ParseResult.ok(new VertexCharacteristicsSelector(context, selectorData.getResult(), inverted));
    }

    /**
     * Returns, whether the variable conditional selector is inverted
     * @return Returns true, if the variable conditional selector is inverted. Otherwise, this method returns false
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Returns, whether the vertex characteristics selector is recursive
     * @return Returns true, if the vertex characteristics selector is recursive. Otherwise, this method returns false
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Returns the vertex characteristic stored in the vertex characteristic selector
     * @return Returns the {@link CharacteristicsSelectorData} stored in the selector
     */
    public CharacteristicsSelectorData getVertexCharacteristics() {
        return vertexCharacteristics;
    }
}
