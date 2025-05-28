package org.dataflowanalysis.examplemodels.results;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public interface Identifier {
    boolean matches(AbstractVertex<?> vertex);
}
