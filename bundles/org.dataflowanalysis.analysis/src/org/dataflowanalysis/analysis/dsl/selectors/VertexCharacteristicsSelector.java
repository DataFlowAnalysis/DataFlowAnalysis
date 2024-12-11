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

public class VertexCharacteristicsSelector extends DataSelector {
    private static final Logger logger = Logger.getLogger(VertexCharacteristicsSelector.class);

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

    @Override
    public String toString() {
        if (this.inverted) {
            return "!" + this.vertexCharacteristics.toString();
        } else {
            return this.vertexCharacteristics.toString();
        }
    }

    public static ParseResult<VertexCharacteristicsSelector> fromString(StringView string, DSLContext context) {
        logger.info("Parsing: " + string.getString());
        boolean inverted = string.getString().startsWith("!");
        ParseResult<CharacteristicsSelectorData> selectorData = CharacteristicsSelectorData.fromString(string);
        if (selectorData.failed()) {
            return ParseResult.error(selectorData.getError());
        }
        if (inverted) string.advance(1);
        string.advance(1);
        return ParseResult.ok(new VertexCharacteristicsSelector(context, selectorData.getResult(), inverted));
    }
}
