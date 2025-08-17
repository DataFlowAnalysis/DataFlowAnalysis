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
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

/**
 * Represents an analysis constraint created by the DSL
 */
public class AnalysisConstraint {
    private static final String DSL_LIST_TOKEN = "-";
    private static final String DSL_NAME_SEPARATOR = ":";
    private static final String DSL_KEYWORD = "neverFlows";

    private static final String FAILED_MATCHING_MESSAGE = "Vertex %s failed to match selector %s";
    private static final String SUCCEEDED_MATCHING_MESSAGE = "Vertex %s matched all selectors";
    private static final String OMMITED_TRANSPOSE_FLOW_GRAPH = "Transpose flow graph %s did not contain any violations. Omitting!";

    private final Logger logger = Logger.getLogger(AnalysisConstraint.class);
    private final String name;
    private final DataSourceSelectors dataSourceSelectors;
    private final VertexSourceSelectors vertexSourceSelectors;
    private final VertexDestinationSelectors vertexDestinationSelectors;
    private final ConditionalSelectors conditionalSelectors;
    private final DSLContext context;

    /**
     * Create a new analysis constraint with no constraints
     */
    public AnalysisConstraint(String name) {
        this.name = name;
        this.vertexSourceSelectors = new VertexSourceSelectors();
        this.dataSourceSelectors = new DataSourceSelectors();
        this.vertexDestinationSelectors = new VertexDestinationSelectors();
        this.conditionalSelectors = new ConditionalSelectors();
        this.context = new DSLContext();
    }

    public AnalysisConstraint(String name, VertexSourceSelectors vertexSourceSelectors, DataSourceSelectors dataSourceSelectors,
            VertexDestinationSelectors vertexDestinationSelectors, ConditionalSelectors conditionalSelectors, DSLContext context) {
        this.name = name;
        this.vertexSourceSelectors = vertexSourceSelectors;
        this.dataSourceSelectors = dataSourceSelectors;
        this.vertexDestinationSelectors = vertexDestinationSelectors;
        this.conditionalSelectors = conditionalSelectors;
        this.context = context;
    }

    /**
     * Find violations of the constraint in the given flow graph collection
     * @param flowGraphCollection Given flow graph collection in which the constraint is evaluated
     * @return Returns a list of dsl results for each <b>violating</b> transpose flow graph
     */
    public List<DSLResult> findViolations(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                for (AbstractSelector selector : Stream.concat(Stream.concat(dataSourceSelectors.getSelectors()
                        .stream(),
                        vertexSourceSelectors.getSelectors()
                                .stream()),
                        vertexDestinationSelectors.getSelectors()
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

    /**
     * Adds a data source selector to the constraint
     * @param selector Data source selector that is added to the constraint
     */
    public void addDataSourceSelector(AbstractSelector selector) {
        this.dataSourceSelectors.addSelector(selector);
    }

    /**
     * Adds a node source selector to the constraint
     * @param selector Node source selector that is added to the constraint
     */
    public void addNodeSourceSelector(AbstractSelector selector) {
        this.vertexSourceSelectors.addSelector(selector);
    }

    /**
     * Adds a flow destination selector to the constraint
     * @param selector Flow destination selector that is added to the constraint
     */
    public void addNodeDestinationSelector(AbstractSelector selector) {
        this.vertexDestinationSelectors.addSelector(selector);
    }

    /**
     * Adds a conditional selector to the constraint
     * @param selector Conditional selector that is added to the constraint
     */
    public void addConditionalSelector(ConditionalSelector selector) {
        this.conditionalSelectors.addSelector(selector);
    }

    /**
     * Returns the context of constraint variables of the constraint
     * @return Constraint variable context of the constraint
     */
    public DSLContext getContext() {
        return context;
    }

    public DataSourceSelectors getDataSourceSelectors() {
        return dataSourceSelectors;
    }

    public VertexSourceSelectors getVertexSourceSelectors() {
        return vertexSourceSelectors;
    }

    public VertexDestinationSelectors getVertexDestinationSelectors() {
        return vertexDestinationSelectors;
    }

    public ConditionalSelectors getConditionalSelectors() {
        return conditionalSelectors;
    }

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        dslString.add(DSL_LIST_TOKEN);
        dslString.add(this.name + DSL_NAME_SEPARATOR);
        SourceSelectors sourceSelectors = new SourceSelectors(this.dataSourceSelectors, this.vertexSourceSelectors);
        dslString.add(sourceSelectors.toString());
        dslString.add(DSL_KEYWORD);
        if (!this.vertexDestinationSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.vertexDestinationSelectors.toString());
        }
        if (!this.conditionalSelectors.getSelectors()
                .isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    /**
     * Parses an analysis constraint from a given string view without a context provider
     * @param string View on the parsed string
     * @return Returns a {@link ParseResult} that may contain the {@link AnalysisConstraint}
     */
    public static ParseResult<AnalysisConstraint> fromString(StringView string) {
        return AnalysisConstraint.fromString(string, null);
    }

    /**
     * Parses an analysis constraint from a given string view with a context provider
     * @param string View on the parsed string
     * @param contextProvider Context provider used to parse analysis-specific contents
     * @return Returns a {@link ParseResult} that may contain the {@link AnalysisConstraint}
     */
    public static ParseResult<AnalysisConstraint> fromString(StringView string, DSLContextProvider contextProvider) {
        DSLContext context = new DSLContext(contextProvider);
        string.skipWhitespace();
        if (!string.startsWith(DSL_LIST_TOKEN)) {
            return string.expect(DSL_LIST_TOKEN);
        }
        string.advance(DSL_LIST_TOKEN.length() + 1);
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
        DataSourceSelectors dataSourceSelectors = sourceSelectors.getResult()
                .getDataSourceSelectors()
                .orElse(new DataSourceSelectors());
        VertexSourceSelectors vertexSourceSelectors = sourceSelectors.getResult()
                .getVertexSourceSelectors()
                .orElse(new VertexSourceSelectors());
        string.skipWhitespace();
        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);

        string.skipWhitespace();
        if (string.empty()) {
            return ParseResult.ok(new AnalysisConstraint(name, vertexSourceSelectors, dataSourceSelectors, new VertexDestinationSelectors(),
                    new ConditionalSelectors(), context));
        }

        ParseResult<VertexDestinationSelectors> nodeDestinationSelectorsParseResult = VertexDestinationSelectors.fromString(string, context);
        if (nodeDestinationSelectorsParseResult.failed()) {
            return ParseResult.error(nodeDestinationSelectorsParseResult.getError());
        }
        VertexDestinationSelectors vertexDestinationSelectors = nodeDestinationSelectorsParseResult.getResult();

        string.skipWhitespace();
        ParseResult<ConditionalSelectors> conditionalSelectorsParseResult = ConditionalSelectors.fromString(string, context);
        ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult.or(new ConditionalSelectors());

        string.skipWhitespace();
        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }
        return ParseResult.ok(
                new AnalysisConstraint(name, vertexSourceSelectors, dataSourceSelectors, vertexDestinationSelectors, conditionalSelectors, context));
    }

    public String getName() {
        return name;
    }
}
