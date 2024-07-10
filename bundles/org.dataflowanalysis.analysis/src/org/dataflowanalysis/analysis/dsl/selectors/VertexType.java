package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public interface VertexType {
    boolean matches(AbstractVertex<?> vertex);
}
