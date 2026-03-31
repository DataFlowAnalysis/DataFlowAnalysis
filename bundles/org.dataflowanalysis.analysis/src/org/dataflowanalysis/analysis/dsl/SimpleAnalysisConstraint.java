package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors;
import org.dataflowanalysis.analysis.dsl.groups.DestinationSelectors;
import org.dataflowanalysis.analysis.dsl.groups.SourceSelectors;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

public class SimpleAnalysisConstraint extends AnalysisConstraint {
    private final Logger logger = LoggerManager.getLogger(SimpleAnalysisConstraint.class);

    /**
     * Create a new analysis constraint with no constraints
     */
    public SimpleAnalysisConstraint(String name) {
        super(name);
    }

    public SimpleAnalysisConstraint(String name, org.dataflowanalysis.analysis.dsl.groups.SourceSelectors sourceSelectors,
            DestinationSelectors destinationSelectors, org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors,
            DSLContext context) {
        super(name, sourceSelectors, FlowType.NEVER_FLOWS, destinationSelectors, conditionalSelectors, context);
    }

    public List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                for (AbstractSelector selector : Stream.concat(super.sourceSelectors.getSelectors()
                        .stream(),
                        super.destinationSelectors.getSelectors()
                                .stream())
                        .toList()) {
                    if (!selector.matches(vertex)) {
                        logger.debug(String.format(FAILED_MATCHING_MESSAGE, vertex, selector));
                        matched = false;
                        constraintTrace.addMissingSelector(vertex, selector);
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
        dslString.add(SIMPLE_DSL_TOKEN);
        dslString.add(this.name + DSL_NAME_SEPARATOR);
        dslString.add(sourceSelectors.toString());
        dslString.add(FlowType.NEVER_FLOWS.toString());
        dslString.add(destinationSelectors.toString());
        if (!this.conditionalSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    public static ParseResult<SimpleAnalysisConstraint> fromString(StringView string, DSLContextProvider contextProvider) {
        DSLContext context = new DSLContext(contextProvider);
        string.skipWhitespace();
        if (!string.startsWith(SIMPLE_DSL_TOKEN)) {
            return string.expect(SIMPLE_DSL_TOKEN);
        }
        string.advance(SIMPLE_DSL_TOKEN.length() + 1);
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
        var sourceSelectors = SourceSelectors.fromString(string, context);
        if (sourceSelectors.failed()) {
            return ParseResult.error(sourceSelectors.getError());
        }
        string.skipWhitespace();
        var flowType = FlowType.fromString(string);
        if (flowType.failed() || !flowType.getResult()
                .equals(FlowType.NEVER_FLOWS)) {
            return string.expect(FlowType.NEVER_FLOWS.toString());
        }
        string.skipWhitespace();
        if (string.empty()) {
            return ParseResult.ok(new SimpleAnalysisConstraint(name, sourceSelectors.getResult(), new DestinationSelectors(),
                    new org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors(), context));
        }

        ParseResult<DestinationSelectors> destinationSelectorsParseResult = DestinationSelectors.fromString(string, context);
        if (destinationSelectorsParseResult.failed()) {
            return ParseResult.error(destinationSelectorsParseResult.getError());
        }
        DestinationSelectors destinationSelectors = destinationSelectorsParseResult.getResult();

        string.skipWhitespace();
        ParseResult<org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors> conditionalSelectorsParseResult = org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors
                .fromString(string, context);
        org.dataflowanalysis.analysis.dsl.groups.ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult
                .or(new ConditionalSelectors());

        string.skipWhitespace();
        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }
        if (!sourceSelectors.getResult()
                .getVertexSourceSelectors()
                .getSelectors()
                .isEmpty()
                && !destinationSelectors.getSelectors()
                        .isEmpty()
                && sourceSelectors.getResult()
                        .getDataSourceSelectors()
                        .getSelectors()
                        .isEmpty()) {
            return ParseResult.error("Cannot create DSL constraint from purely vertex selectors! This behavior is not implemented yet!");
        }
        return ParseResult.ok(new SimpleAnalysisConstraint(name, sourceSelectors.getResult(), destinationSelectors, conditionalSelectors, context));
    }

}
