package org.dataflowanalysis.analysis.dsl.selectors.vertex;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class VertexSelector extends AbstractSelector {
    private static final String DSL_KEYWORD = "vertex";

    public VertexSelector(DSLContext context) {
        super(context);
    }

    public static ParseResult<VertexSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        int position = string.getPosition();
        if (string.empty() || string.invalid()) {
            return ParseResult.error("Not a valid constraint");
        }
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
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
        string.setPosition(position);
        return ParseResult.error("Not a valid constraint");
    }

    @Override
    public boolean hasDataSelector() {
        return false;
    }

    @Override
    public boolean hasVertexSelector() {
        return true;
    }

    @Override
    public String toString() {
        return DSL_KEYWORD;
    }
}
