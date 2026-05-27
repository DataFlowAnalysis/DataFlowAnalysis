package org.dataflowanalysis.analysis.dsl.selectors.data;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class DataSelector extends AbstractSelector {
    private static final String DSL_KEYWORD = "data";

    public DataSelector(DSLContext context) {
        super(context);
    }

    @Override
    public boolean hasDataSelector() {
        return true;
    }

    @Override
    public boolean hasVertexSelector() {
        return false;
    }

    public static ParseResult<DataSelector> fromString(StringView string, DSLContext context) {
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
        var nameSelector = VariableNameSelector.fromString(string, context);
        if (nameSelector.successful()) {
            return ParseResult.ok(nameSelector.getResult());
        }
        var listSelector = DataCharacteristicListSelector.fromString(string, context);
        if (listSelector.successful()) {
            return ParseResult.ok(listSelector.getResult());
        }
        var selector = DataCharacteristicsSelector.fromString(string, context);
        if (selector.successful()) {
            return ParseResult.ok(selector.getResult());
        }
        string.setPosition(position);
        return ParseResult.error("Not a valid constraint");
    }

    @Override
    public String toString() {
        return DSL_KEYWORD;
    }
}
