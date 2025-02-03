package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexTypeSelector extends VertexSelector {
    private static final Logger logger = Logger.getLogger(VertexTypeSelector.class);
    private static final String DSL_KEYWORD = "type";

    private final VertexType vertexType;
    private final boolean inverted;
    private final boolean recursive;

    public VertexTypeSelector(DSLContext context, VertexType vertexType) {
        super(context);
        this.vertexType = vertexType;
        this.inverted = false;
        this.recursive = false;
    }

    public VertexTypeSelector(DSLContext context, VertexType vertexType, boolean inverted) {
        super(context);
        this.vertexType = vertexType;
        this.inverted = inverted;
        this.recursive = false;
    }

    public VertexTypeSelector(DSLContext context, VertexType vertexType, boolean inverted, boolean recursive) {
        super(context);
        this.vertexType = vertexType;
        this.inverted = inverted;
        this.recursive = recursive;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        if (this.recursive) {
            return this.inverted
                    ? !this.vertexType.matches(vertex) && vertex.getPreviousElements().stream().noneMatch(this::matches)
                    : this.vertexType.matches(vertex) || vertex.getPreviousElements().stream().anyMatch(this::matches);
        }
        return this.inverted
                ? !this.vertexType.matches(vertex)
                : this.vertexType.matches(vertex);
    }

    @Override
    public String toString() {
        if (this.inverted) {
            return DSL_KEYWORD + " " + DSL_INVERTED_SYMBOL + this.vertexType.toString();
        } else {
            return DSL_KEYWORD + " " + this.vertexType.toString();
        }
    }

    public static ParseResult<VertexTypeSelector> fromString(StringView string, DSLContext context) {
        logger.info("Parsing: " + string.getString());
        boolean inverted = string.getString().startsWith(DSL_INVERTED_SYMBOL);
        if (context.getContextProvider().isEmpty()) {
        	return ParseResult.error("Cannot parse vertex types without context provider!");
        }
        ParseResult<VertexType> vertexType = context.getContextProvider().get().vertexTypeFromString(string);
        if (vertexType.failed()) {
            return ParseResult.error(vertexType.getError());
        }
        if (inverted) string.advance(DSL_INVERTED_SYMBOL.length());
        string.advance(1);
        return ParseResult.ok(new VertexTypeSelector(context, vertexType.getResult(), inverted));
    }
}
