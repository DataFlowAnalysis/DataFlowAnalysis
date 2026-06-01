package org.dataflowanalysis.analysis.dsl.selectors.flow;

import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;

public class AlwaysFlowType extends AbstractFlowType {
    private static final Logger logger = LoggerManager.getLogger(AlwaysFlowType.class);

    public AlwaysFlowType(AbstractSelector sourceSelector, AbstractSelector destinationSelector,
            List<ConditionalSelector> conditionalSelectors, FlowGraphCollection flowGraphCollection) {
        super(sourceSelector, destinationSelector, conditionalSelectors, flowGraphCollection);
    }

    @Override
    public boolean evaluateFlowType(AbstractVertex<?> vertex, DSLConstraintTrace constraintTrace, DSLContext context) {
        boolean matched = true;
        if (this.sourceSelector.hasDataSelector() && destinationSelector.hasDataSelector()) {
            var alternateNodes = flowGraphCollection.getTransposeFlowGraphs()
                    .stream()
                    .map(AbstractTransposeFlowGraph::getVertices)
                    .flatMap(Collection::stream)
                    .filter(it -> it.getReferencedElement()
                            .equals(vertex.getReferencedElement()))
                    .toList();
            if (!sourceSelector.matchesSource(vertex, constraintTrace)) {
                matched = false;
            }

            if (alternateNodes.isEmpty()) {
                matched = false;
            }
            boolean matchedAlternate = false;
            for (AbstractVertex<?> alternateVertex : alternateNodes) {
                boolean matchedAlternateVertex = destinationSelector.matchesSource(alternateVertex, constraintTrace);
                if (!matchedAlternateVertex) {
                    matchedAlternate = true;
                }
            }
            if (!matchedAlternate) {
                matched = false;
            }
        } else {
            if (!sourceSelector.matchesSource(vertex, constraintTrace)) {
                matched = false;
            }
            if (destinationSelector.matchesDestination(vertex, constraintTrace)) {
                matched = false;
            }
        }
        for (ConditionalSelector selector : conditionalSelectors) {
            if (!selector.matchesSelector(vertex, context)) {
                logger.debug(String.format(FAILED_MATCHING_MESSAGE, vertex, selector));
                matched = false;
                constraintTrace.addMissingConditionalSelector(vertex, selector);
            }
        }
        if (matched) {
            logger.debug(String.format(SUCCEEDED_MATCHING_MESSAGE, vertex));
        }
        return matched;
    }
}
