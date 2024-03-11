package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public class VertexTypeSelector extends VertexSelector {
    private final VertexType vertexType;
    private final boolean inverted;

    public VertexTypeSelector(VertexType vertexType) {
        this.vertexType = vertexType;
        this.inverted = false;
    }

    public VertexTypeSelector(VertexType vertexType, boolean inverted) {
        this.vertexType = vertexType;
        this.inverted = inverted;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return this.vertexType.matches(vertex);
    }
}
