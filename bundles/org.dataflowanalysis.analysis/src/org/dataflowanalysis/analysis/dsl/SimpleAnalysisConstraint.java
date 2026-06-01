package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
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
import org.dataflowanalysis.analysis.dsl.selectors.flow.AbstractFlowType;
import org.dataflowanalysis.analysis.dsl.selectors.flow.FlowType;
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

    public SimpleAnalysisConstraint(String name, AbstractSelector sourceSelectors,
            AbstractSelector destinationSelectors, ConditionalSelectors conditionalSelectors, DSLContext context) {
        super(name, sourceSelectors, FlowType.NEVER_FLOWS, destinationSelectors, conditionalSelectors, context);
    }

    public List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                AbstractFlowType instantiatedFlowType = this.flowType.instantiate(sourceSelector, destinationSelector,
                        conditionalSelectors.getSelectors(), flowGraphCollection);
                boolean matched = instantiatedFlowType.evaluateFlowType(vertex, constraintTrace, context);
                if (matched) {
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
        dslString.add(sourceSelector.toString());
        dslString.add(FlowType.NEVER_FLOWS.toString());
        dslString.add(destinationSelector.toString());
        if (!this.conditionalSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    public static ParseResult<SimpleAnalysisConstraint> fromString(StringView string,
            DSLContextProvider contextProvider) {
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

        var sourceSelector = AbstractSelector.fromString(string, context);
        if (sourceSelector.failed()) {
            return ParseResult.error(sourceSelector.getError());
        }
        string.skipWhitespace();
        var flowType = FlowType.fromString(string);
        if (flowType.failed() || !flowType.getResult()
                .equals(FlowType.NEVER_FLOWS)) {
            return string.expect(FlowType.NEVER_FLOWS.toString());
        }
        string.skipWhitespace();
        if (string.empty()) {
            return ParseResult.ok(new SimpleAnalysisConstraint(name, sourceSelector.getResult(),
                    new AnySelector(context), new ConditionalSelectors(), context));
        }

        var destinationSelector = AbstractSelector.fromString(string, context);
        if (destinationSelector.failed()) {
            return ParseResult.error(destinationSelector.getError());
        }
        AbstractSelector destinationSelectors = destinationSelector.getResult();

        string.skipWhitespace();
        ParseResult<ConditionalSelectors> conditionalSelectorsParseResult = ConditionalSelectors.fromString(string,
                context);
        ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult.or(new ConditionalSelectors());

        string.skipWhitespace();
        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }
        if (!sourceSelector.getResult()
                .hasVertexSelector()
                && !(destinationSelectors.hasDataSelector() || sourceSelector.getResult()
                        .hasDataSelector())) {
            return ParseResult.error(
                    "Cannot create DSL constraint from purely vertex selectors! This behavior is not implemented yet!");
        }
        return ParseResult.ok(new SimpleAnalysisConstraint(name, sourceSelector.getResult(), destinationSelectors,
                conditionalSelectors, context));
    }

}
