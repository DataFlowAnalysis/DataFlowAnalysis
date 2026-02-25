package org.dataflowanalysis.analysis.dsl.selectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class VertexTypeSelector extends VertexSelector {
    private static final Logger logger = LoggerManager.getLogger(VertexTypeSelector.class);
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
            return this.inverted ? !this.vertexType.matches(vertex) && vertex.getPreviousElements()
                    .stream()
                    .noneMatch(this::matches)
                    : this.vertexType.matches(vertex) || vertex.getPreviousElements()
                            .stream()
                            .anyMatch(this::matches);
        }
        return this.inverted ? !this.vertexType.matches(vertex) : this.vertexType.matches(vertex);
    }

    @Override
    public String toString() {
        if (this.inverted) {
            return DSL_KEYWORD + " " + DSL_INVERTED_SYMBOL + this.vertexType.toString();
        } else {
            return DSL_KEYWORD + " " + this.vertexType.toString();
        }
    }

    /**
     * Parses a {@link VertexTypeSelector} object from the given view on a string
     * <p/>
     * This method expects the following format: {@code type <VertexType>}
     * @param string String view on the string that is parsed
     * @return {@link ParseResult} containing the {@link VertexTypeSelector} object
     */
    public static ParseResult<VertexTypeSelector> fromString(StringView string, DSLContext context) {
        string.skipWhitespace();
        if (string.invalid() || string.empty()) {
            return ParseResult.error("Cannot parse vertex type selector from empty or invalid string!");
        }
        logger.debug("Parsing: " + string.getString());
        int position = string.getPosition();
        boolean inverted = string.getString()
                .startsWith(DSL_INVERTED_SYMBOL);
        if (context.getContextProvider()
                .isEmpty()) {
            return ParseResult.error("Cannot parse vertex types without context provider!");
        }
        if (inverted) {
            string.advance(DSL_INVERTED_SYMBOL.length());
        }
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);
        ParseResult<VertexType> vertexType = context.getContextProvider()
                .get()
                .vertexTypeFromString(string);
        if (vertexType.failed()) {
            string.setPosition(position);
            return ParseResult.error(vertexType.getError());
        }
        if (inverted)
            string.advance(DSL_INVERTED_SYMBOL.length());
        string.advance(1);
        return ParseResult.ok(new VertexTypeSelector(context, vertexType.getResult(), inverted));
    }

    /**
     * Returns the {@link VertexType} that is matched by the selector
     * @return Returns the stored {@link VertexType}
     */
    public VertexType getVertexType() {
        return vertexType;
    }

    /**
     * Returns, whether the vertex type selector is inverted
     * @return Returns true, if the vertex type selector is inverted. Otherwise, this method returns false
     */
    public boolean isInverted() {
        return inverted;
    }

    /**
     * Returns, whether the vertex type selector is recursive
     * @return Returns true, if the vertex type selector is recursive. Otherwise, this method returns false
     */
    public boolean isRecursive() {
        return recursive;
    }
}
