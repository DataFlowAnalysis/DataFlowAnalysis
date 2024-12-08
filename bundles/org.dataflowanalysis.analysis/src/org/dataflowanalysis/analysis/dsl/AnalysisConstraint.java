package org.dataflowanalysis.analysis.dsl;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents an analysis constraint created by the DSL
 */
public class AnalysisConstraint {
    private static final String DSL_CONNECTOR = "neverFlows";

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
        StringBuilder dslString = new StringBuilder();
        dslString.append(this.dataSourceSelectors.toString());
        dslString.append(this.nodeSourceSelectors.toString());
        dslString.append(DSL_CONNECTOR);
        dslString.append(this.nodeDestinationSelectors.toString());
        dslString.append(this.conditionalSelectors.toString());
        return dslString.toString();
    }

    public static AnalysisConstraint fromString(String string) {
        return new AnalysisConstraint();
    }
}
