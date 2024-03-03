package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public class VertexTypeSelector extends VertexSelector {
    private final VertexType vertexType;

    public VertexTypeSelector(VertexType vertexType) {
        this.vertexType = vertexType;
    }

    @Override
    public boolean matches(AbstractVertex<?> vertex) {
        return this.vertexType.matches(vertex);
    }
}
