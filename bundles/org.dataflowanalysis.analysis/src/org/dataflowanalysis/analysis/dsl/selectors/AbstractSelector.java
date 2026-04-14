package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * An abstract representation of a selector with a given {@link DSLContext}. An {@link AbstractSelector} must provide a
 * {@link AbstractSelector#matches(AbstractVertex)} that indicates whether the provide vertex matches the selector
 */
public abstract class AbstractSelector extends AbstractParseable {
    protected static final String DSL_CONTAINS = "contains";

    protected DSLContext context;

    /**
     * Creates a new selector with the given {@link DSLContext}
     * @param context Given {@link DSLContext} of the selector
     */
    public AbstractSelector(DSLContext context) {
        this.context = context;
    }

    /**
     * Determines whether the selector matches the given vertex
     * @param vertex {@link AbstractVertex} that is matched
     * @return Returns true, if the selector matches the vertex. Otherwise, the method returns false
     */
    public abstract boolean matches(AbstractVertex<?> vertex);

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context,
            boolean data) {
        if (string.empty() || string.invalid()) {
            return ParseResult.error("Not a valid constraint");
        }
        string.skipWhitespace();
        if (data) {
            return DataSelector.fromString(string, context);
        } else {
            return VertexSelector.fromString(string, context);
        }
    }
}
