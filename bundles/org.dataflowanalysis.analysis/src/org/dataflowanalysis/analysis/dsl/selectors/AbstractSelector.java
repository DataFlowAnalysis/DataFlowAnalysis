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

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context) {
        if (string.empty() || string.invalid()) {
            return ParseResult.error("Not a valid constraint");
        }
        string.skipWhitespace();
        var nameSelector = VariableNameSelector.fromString(string, context);
        if (nameSelector.successful()) {
            return ParseResult.ok(nameSelector.getResult());
        }
        var vertexNameSelector = VertexNameSelector.fromString(string, context);
        if (vertexNameSelector.successful()) {
            return ParseResult.ok(vertexNameSelector.getResult());
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
