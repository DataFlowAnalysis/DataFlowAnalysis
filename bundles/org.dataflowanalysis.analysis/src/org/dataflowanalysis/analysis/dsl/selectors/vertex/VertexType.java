package org.dataflowanalysis.analysis.dsl.selectors.vertex;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public interface VertexType {
    boolean matches(AbstractVertex<?> vertex);

    String toString();
}
