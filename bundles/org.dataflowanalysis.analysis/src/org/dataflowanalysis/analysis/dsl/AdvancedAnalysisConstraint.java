package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.logic.LogicalOperator;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class AdvancedAnalysisConstraint extends AnalysisConstraint {
    private static final Logger logger = LoggerManager.getLogger(AdvancedAnalysisConstraint.class);

    public AdvancedAnalysisConstraint(String name) {
        super(name);
    }

    public AdvancedAnalysisConstraint(String name, AbstractSelector sourceSelectors, FlowType flowType,
            AbstractSelector destinationSelectors, ConditionalSelectors conditionalSelectors, DSLContext context) {
        super(name, sourceSelectors, flowType, destinationSelectors, conditionalSelectors, context);
    }

    @Override
    public List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                if (this.getSourceSelector()
                        .hasDataSelector()
                        && this.getDestinationSelector()
                                .hasDataSelector()) {
                    var alternateNodes = flowGraphCollection.getTransposeFlowGraphs()
                            .stream()
                            .map(AbstractTransposeFlowGraph::getVertices)
                            .flatMap(Collection::stream)
                            .filter(it -> it.getReferencedElement()
                                    .equals(vertex.getReferencedElement()))
                            .toList();
                    if (!this.sourceSelector.matchesSource(vertex, constraintTrace)) {
                        matched = false;
                    }

                    if (alternateNodes.isEmpty()) {
                        matched = false;
                    }
                    boolean matchedAlternate = false;
                    for (AbstractVertex<?> alternateVertex : alternateNodes) {
                        // TODO: This should only evaluate the data destination selectors, right?
                        boolean matchedAlternateVertex = this.getDestinationSelector()
                                .matchesSource(alternateVertex, constraintTrace);
                        if (matchedAlternateVertex) {
                            matchedAlternate = true;
                        }
                    }
                    if (!matchedAlternate) {
                        matched = false;
                    }
                } else {
                    if (!this.getSourceSelector()
                            .matchesSource(vertex, constraintTrace)) {
                        matched = false;
                    }
                    if (!this.getDestinationSelector()
                            .matchesDestination(vertex, constraintTrace)) {
                        matched = false;
                    }
                }
                for (ConditionalSelector selector : this.conditionalSelectors.getSelectors()) {
                    if (!selector.matchesSelector(vertex, context)) {
                        logger.debug(String.format(FAILED_MATCHING_MESSAGE, vertex, selector));
                        matched = false;
                        constraintTrace.addMissingConditionalSelector(vertex, selector);
                    }
                }
                if (matched) {
                    logger.debug(String.format(SUCCEEDED_MATCHING_MESSAGE, vertex));
                    violations.add(vertex);
                }
            }
            if (!violations.isEmpty()) {
                results.add(new DSLResult(transposeFlowGraph, violations, constraintTrace));
            } else {
                logger.debug(String.format(OMMITED_TRANSPOSE_FLOW_GRAPH, transposeFlowGraph));
            }
        }
        return results;
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        dslString.add(ADVANCED_DSL_TOKEN);
        dslString.add(this.name + DSL_NAME_SEPARATOR);
        dslString.add(sourceSelector.toString());
        dslString.add(flowType.toString());
        dslString.add(destinationSelector.toString());
        if (!this.conditionalSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    public static ParseResult<AdvancedAnalysisConstraint> fromString(StringView string,
            DSLContextProvider contextProvider) {
        DSLContext context = new DSLContext(contextProvider);
        string.skipWhitespace();
        if (!string.startsWith(ADVANCED_DSL_TOKEN)) {
            return string.expect(ADVANCED_DSL_TOKEN);
        }
        string.advance(ADVANCED_DSL_TOKEN.length() + 1);
        string.skipWhitespace();
        int index = string.getString()
                .indexOf(DSL_NAME_SEPARATOR);
        if (index == -1) {
            return ParseResult.error("Invalid DSL Constraint: Did delimit constraint name with " + DSL_NAME_SEPARATOR);
        }
        String name = string.getString()
                .substring(0, index);
        string.advance(name.length());
        if (!string.startsWith(DSL_NAME_SEPARATOR)) {
            return string.expect(DSL_NAME_SEPARATOR);
        }
        string.advance(DSL_NAME_SEPARATOR.length() + 1);
        string.skipWhitespace();

        var sourceSelector = LogicalOperator.fromString(string, context);
        if (sourceSelector.failed()) {
            return ParseResult.error(sourceSelector.getError());
        }

        string.skipWhitespace();
        var flowType = FlowType.fromString(string);
        if (flowType.failed()) {
            return ParseResult.error(flowType.getError());
        }

        string.skipWhitespace();
        if (string.empty()) {
            return ParseResult.ok(new AdvancedAnalysisConstraint(name, sourceSelector.getResult(), flowType.getResult(),
                    new AnySelector(context), new ConditionalSelectors(), context));
        }

        var destinationSelector = LogicalOperator.fromString(string, context);
        if (destinationSelector.failed()) {
            return ParseResult.error(destinationSelector.getError());
        }

        string.skipWhitespace();
        ParseResult<ConditionalSelectors> conditionalSelectorsParseResult = ConditionalSelectors.fromString(string,
                context);
        ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult.or(new ConditionalSelectors());

        string.skipWhitespace();
        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }
        return ParseResult.ok(new AdvancedAnalysisConstraint(name, sourceSelector.getResult(), flowType.getResult(),
                destinationSelector.getResult(), conditionalSelectors, context));
    }
}
