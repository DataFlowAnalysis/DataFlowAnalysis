package org.dataflowanalysis.analysis.dsl;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.context.DSLContextProvider;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.DataCharacteristicsSelector;
import org.dataflowanalysis.analysis.utils.ParseResult;
import org.dataflowanalysis.analysis.utils.StringView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Represents an analysis constraint created by the DSL
 */
public class AnalysisConstraint {
    private static final String DSL_KEYWORD= "neverFlows";

	private static final String FAILED_MATCHING_MESSAGE = "Vertex %s failed to match selector %s";
	private static final String SUCEEDED_MATCHING_MESSAGE = "Vertex %s matched all selectors";
	private static final String OMMITED_TRANSPOSE_FLOW_GRAPH = "Transpose flow graph %s did not contain any violations. Omitting!";
	
    private final Logger logger = Logger.getLogger(AnalysisConstraint.class);
    private final DataSourceSelectors dataSourceSelectors;
    private final NodeSourceSelectors nodeSourceSelectors;
    private final NodeDestinationSelectors nodeDestinationSelectors;
    private final ConditionalSelectors conditionalSelectors;
    private final DSLContext context;

    /**
     * Create a new analysis constraint with no constraints
     */
    public AnalysisConstraint() {
        this.nodeSourceSelectors = new NodeSourceSelectors();
        this.dataSourceSelectors = new DataSourceSelectors();
        this.nodeDestinationSelectors = new NodeDestinationSelectors();
        this.conditionalSelectors = new ConditionalSelectors();
        this.context = new DSLContext();
    }

    public AnalysisConstraint(NodeSourceSelectors nodeSourceSelectors, DataSourceSelectors dataSourceSelectors,
                              NodeDestinationSelectors nodeDestinationSelectors, ConditionalSelectors conditionalSelectors,
                              DSLContext context) {
        this.nodeSourceSelectors = nodeSourceSelectors;
        this.dataSourceSelectors = dataSourceSelectors;
        this.nodeDestinationSelectors = nodeDestinationSelectors;
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
        for(AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                for (AbstractSelector selector : Stream.concat(Stream.concat(dataSourceSelectors.getSelectors().stream(), nodeSourceSelectors.getSelectors().stream()), nodeDestinationSelectors.getSelectors().stream()).toList()) {
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
                	logger.debug(String.format(SUCEEDED_MATCHING_MESSAGE, vertex));
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
        this.nodeSourceSelectors.addSelector(selector);
    }

    /**
     * Adds a flow destination selector to the constraint
     * @param selector Flow destination selector that is added to the constraint
     */
    public void addNodeDestinationSelector(AbstractSelector selector) {
        this.nodeDestinationSelectors.addSelector(selector);
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

    @Override
    public String toString() {
        StringJoiner dslString = new StringJoiner(" ");
        if (!this.dataSourceSelectors.getSelectors().isEmpty()) {
            dslString.add(this.dataSourceSelectors.toString());
        }
        if (!this.nodeSourceSelectors.getSelectors().isEmpty()) {
            dslString.add(this.nodeSourceSelectors.toString());
        }
        dslString.add(DSL_KEYWORD);
        if (!this.nodeDestinationSelectors.getSelectors().isEmpty()) {
            dslString.add(this.nodeDestinationSelectors.toString());
        }
        if (!this.conditionalSelectors.getSelectors().isEmpty()) {
            dslString.add(this.conditionalSelectors.toString());
        }
        return dslString.toString();
    }

    public static ParseResult<AnalysisConstraint> fromString(StringView string) {
    	return AnalysisConstraint.fromString(string, null);
    }
    
    public static ParseResult<AnalysisConstraint> fromString(StringView string, DSLContextProvider contextProvider) {
        DSLContext context = new DSLContext(contextProvider);
        var sourceSelectors = parseSourceSelector(string, context);
        if (sourceSelectors.failed()) {
            return ParseResult.error(sourceSelectors.getError());
        }
        DataSourceSelectors dataSourceSelectors = sourceSelectors.getResult().getDataSourceSelectors().orElse(new DataSourceSelectors());
        NodeSourceSelectors nodeSourceSelectors = sourceSelectors.getResult().getNodeSourceSelectors().orElse(new NodeSourceSelectors());

        if (!string.startsWith(DSL_KEYWORD)) {
            return string.expect(DSL_KEYWORD);
        }
        string.advance(DSL_KEYWORD.length() + 1);

        ParseResult<NodeDestinationSelectors> nodeDestinationSelectorsParseResult = NodeDestinationSelectors.fromString(string, context);
        if (nodeDestinationSelectorsParseResult.failed()) {
            return ParseResult.error(nodeDestinationSelectorsParseResult.getError());
        }
        NodeDestinationSelectors nodeDestinationSelectors = nodeDestinationSelectorsParseResult.getResult();

        ParseResult<ConditionalSelectors> conditionalSelectorsParseResult = ConditionalSelectors.fromString(string, context);
        ConditionalSelectors conditionalSelectors = conditionalSelectorsParseResult.or(new ConditionalSelectors());

        if (!string.empty()) {
            return ParseResult.error("Unexpected symbols: " + string.getString());
        }

        return ParseResult.ok(new AnalysisConstraint(nodeSourceSelectors, dataSourceSelectors, nodeDestinationSelectors, conditionalSelectors, context));
    }

    public static ParseResult<SourceSelectors> parseSourceSelector(StringView string, DSLContext context) {
        ParseResult<DataSourceSelectors> dataSourceSelector = DataSourceSelectors.fromString(string, context);
        ParseResult<NodeSourceSelectors> nodeSourceSelector;
        if (dataSourceSelector.successful()) {
            nodeSourceSelector = NodeSourceSelectors.fromString(string, context);
        } else {
            nodeSourceSelector = NodeSourceSelectors.fromString(string, context);
            if (nodeSourceSelector.successful())
                dataSourceSelector = DataSourceSelectors.fromString(string, context);
        }

        if (nodeSourceSelector.successful() && dataSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(dataSourceSelector.getResult(), nodeSourceSelector.getResult()));
        } else if(dataSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(dataSourceSelector.getResult()));
        } else if (nodeSourceSelector.successful()) {
            return ParseResult.ok(new SourceSelectors(nodeSourceSelector.getResult()));
        } else {
            return ParseResult.error("Could not parse source selectors");
        }
    }
}
