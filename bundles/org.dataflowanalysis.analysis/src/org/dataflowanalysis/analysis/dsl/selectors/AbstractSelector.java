package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.AbstractParseable;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.data.DataSelector;
import org.dataflowanalysis.analysis.dsl.selectors.vertex.VertexSelector;
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

    public boolean matches(AbstractVertex<?> vertex, SelectorEnvironment selectorEnvironment,
            DSLConstraintTrace dslConstraintTrace) {
        if (selectorEnvironment == SelectorEnvironment.SOURCE_SELECTOR) {
            return this.matchesSource(vertex, dslConstraintTrace);
        } else {
            return this.matchesDestination(vertex, dslConstraintTrace);
        }
    }

    /**
     * Determines whether the selector matches the given vertex
     * @param vertex {@link AbstractVertex} that is matched
     * @return Returns true, if the selector matches the vertex. Otherwise, the method returns false
     */
    public abstract boolean matchesSource(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace);

    /**
     * Determines whether the selector matches the given vertex
     * @param vertex {@link AbstractVertex} that is matched
     * @return Returns true, if the selector matches the vertex. Otherwise, the method returns false
     */
    public abstract boolean matchesDestination(AbstractVertex<?> vertex, DSLConstraintTrace dslConstraintTrace);

    /**
     * Indicates whether the selector has any contained data selector
     * @return Returns true, if the selector contains a data selector. Otherwise, the method will return false
     */
    public abstract boolean hasDataSelector();

    /**
     * Indicates whether the selector has any contained vertex selector
     * @return Returns true, if the selector contains a vertex selector. Otherwise, the method will return false
     */
    public abstract boolean hasVertexSelector();

    public static ParseResult<? extends AbstractSelector> fromString(StringView string, DSLContext context) {
        var dataSelector = DataSelector.fromString(string, context);
        if (dataSelector.successful()) {
            return ParseResult.ok(dataSelector.getResult());
        }
        var vertexSelector = VertexSelector.fromString(string, context);
        if (vertexSelector.successful()) {
            return ParseResult.ok(vertexSelector.getResult());
        }
        var anySelector = AnySelector.fromString(string, context);
        if (anySelector.successful()) {
            return ParseResult.ok(anySelector.getResult());
        }
        return ParseResult.error("Not a valid constraint");
    }
}
