package org.dataflowanalysis.analysis.dsl.selectors;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;

public interface ConditionalSelector {
    boolean matchesSelector(AbstractVertex<?> vertex, DSLContext context);
}
