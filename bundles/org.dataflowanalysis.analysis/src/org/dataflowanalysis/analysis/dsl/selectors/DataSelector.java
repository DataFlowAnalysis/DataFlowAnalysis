package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public abstract class DataSelector extends AbstractSelector {

    public DataSelector(DSLContext context) {
        super(context);
    }

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context) {
        if (string.empty() || string.invalid()) {
            return ParseResult.error("Not a valid constraint");
        }
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
        var anySelector = AnySelector.fromString(string, context);
        if (anySelector.successful()) {
            return ParseResult.ok(anySelector.getResult());
        }
        return ParseResult.error("Not a valid constraint");
    }
}
