package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public class VertexTypeSelector extends VertexSelector {
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
}
