package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;

public abstract class AbstractSelector {
    public abstract boolean matches(AbstractVertex<?> vertex);
}
