package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public abstract class AbstractSelector {
    protected DSLContext context;

    public AbstractSelector(DSLContext context) {
        this.context = context;
    }

    public abstract boolean matches(AbstractVertex<?> vertex);
}
