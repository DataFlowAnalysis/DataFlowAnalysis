package org.dataflowanalysis.analysis.dsl.selectors.flow;

import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;

public abstract class AbstractFlowType {
    protected static final String FAILED_MATCHING_MESSAGE = "Vertex %s failed to match selector %s";
    protected static final String SUCCEEDED_MATCHING_MESSAGE = "Vertex %s matched all selectors";

    protected final AbstractSelector sourceSelector;
    protected final AbstractSelector destinationSelector;
    protected final List<ConditionalSelector> conditionalSelectors;
    protected final FlowGraphCollection flowGraphCollection;

    public AbstractFlowType(AbstractSelector sourceSelector, AbstractSelector destinationSelector,
            List<ConditionalSelector> conditionalSelectors, FlowGraphCollection flowGraphCollection) {
        this.sourceSelector = sourceSelector;
        this.destinationSelector = destinationSelector;
        this.conditionalSelectors = conditionalSelectors;
        this.flowGraphCollection = flowGraphCollection;
    }

    public abstract boolean evaluateFlowType(AbstractVertex<?> vertex, DSLConstraintTrace constraintTrace,
            DSLContext context);
}
