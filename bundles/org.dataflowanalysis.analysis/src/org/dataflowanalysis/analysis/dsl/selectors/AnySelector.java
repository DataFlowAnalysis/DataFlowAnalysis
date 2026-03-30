package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class AnySelector extends AbstractSelector {
    private static final String DSL_KEYWORD = "any";

    private static final Logger logger = LoggerManager.getLogger(AnySelector.class);

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public AnySelector(DSLContext context) {
        super(context);
    }

    @Override
    public String toString() {
        return DSL_KEYWORD;
    }

    /**
     * Parses a {@link AnySelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code any}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link AnySelector} object
     */
    public static ParseResult<AnySelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse any selector from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length());
        return ParseResult.ok(new AnySelector(context));
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return true;
    }
}
