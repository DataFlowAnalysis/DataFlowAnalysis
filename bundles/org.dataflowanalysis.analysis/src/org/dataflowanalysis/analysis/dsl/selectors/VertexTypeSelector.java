package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public class VertexTypeSelector extends VertexSelector {
    private final VertexType vertexType;
    private final boolean inverted;

    public VertexTypeSelector(DSLContext context, VertexType vertexType) {
        super(context);
        this.vertexType = vertexType;
        this.inverted = false;
    }

    public VertexTypeSelector(DSLContext context, VertexType vertexType, boolean inverted) {
        super(context);
        this.vertexType = vertexType;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return this.vertexType.matches(vertex);
    }
}
