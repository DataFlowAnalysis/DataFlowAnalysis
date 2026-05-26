package org.dataflowanalysis.analysis.dsl.selectors;

import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class VertexSelector extends AbstractSelector {
    public VertexSelector(DSLContext context) {
        super(context);
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return this.matches(vertex, vertex.getAllVertexCharacteristics());
    }

    /**
     * Determines whether the selector matches the given vertex
     * @param vertex {@link AbstractVertex} that is matched
     * @return Returns true, if the selector matches the vertex. Otherwise, the method returns false
     */
    public abstract boolean matches(AbstractVertex<?> vertex, List<CharacteristicValue> presentCharacteristics);

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context) {
        if (string.empty() || string.invalid()) {
            return ParseResult.error("Not a valid constraint");
        }
        string.skipWhitespace();
        var vertexCharacteristicsListSelector = VertexCharacteristicsListSelector.fromString(string, context);
        if (vertexCharacteristicsListSelector.successful()) {
            return ParseResult.ok(vertexCharacteristicsListSelector.getResult());
        }
        var vertexCharacteristicsSelector = VertexCharacteristicsSelector.fromString(string, context);
        if (vertexCharacteristicsSelector.successful()) {
            return ParseResult.ok(vertexCharacteristicsSelector.getResult());
        }
        var vertexNameSelector = VertexNameSelector.fromString(string, context);
        if (vertexNameSelector.successful()) {
            return ParseResult.ok(vertexNameSelector.getResult());
        }
        var anySelector = AnySelector.fromString(string, context);
        if (anySelector.successful()) {
            return ParseResult.ok(anySelector.getResult());
        }
        return ParseResult.error("Not a valid constraint");
    }
}
