package org.dataflowanalysis.analysis.dsl;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AnySelector;
import org.dataflowanalysis.analysis.dsl.selectors.SelectorEnvironment;
import org.dataflowanalysis.analysis.dsl.selectors.conditional.ConditionalSelector;
import org.dataflowanalysis.analysis.utils.LoggerManager;

/**
 * Represents an analysis query created by the DSL
 */
public class AnalysisQuery {
    private static final String FAILED_MATCHING_MESSAGE = "Vertex %s failed to match selector %s";
    private static final String SUCCEEDED_MATCHING_MESSAGE = "Vertex %s matched all selectors";
    private static final String OMMITED_TRANSPOSE_FLOW_GRAPH = "Transpose flow graph %s did not contain any queried vertices. Omitting!";

    private final Logger logger = LoggerManager.getLogger(AnalysisQuery.class);
    private AbstractSelector dataSources;
    private AbstractSelector vertexDestinations;
    private final List<ConditionalSelector> selectors;
    private final DSLContext context;

    /**
     * Create a new empty analysis query
     */
    public AnalysisQuery() {
        this.context = new DSLContext();
        this.dataSources = new AnySelector(this.context);
        this.vertexDestinations = new AnySelector(this.context);
        this.selectors = new ArrayList<>();
    }

    /**
     * Find queried vertices of the query in the given flow graph collection
     * @param flowGraphCollection Given flow graph collection in which the query is evaluated
     * @return Returns a list of dsl results for each <b>queried</b> transpose flow graph
     */
    public List<DSLResult> query(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> matchedVertices = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                if (!this.dataSources.matches(vertex, SelectorEnvironment.SOURCE_SELECTOR, constraintTrace)) {
                    matched = false;
                }
                if (!this.vertexDestinations.matches(vertex, SelectorEnvironment.DESTINATION_SELECTOR,
                        constraintTrace)) {
                    matched = false;
                }
                for (ConditionalSelector selector : this.selectors) {
                    if (!selector.matchesSelector(vertex, context)) {
                        logger.debug(String.format(FAILED_MATCHING_MESSAGE, vertex, selector));
                        matched = false;
                        constraintTrace.addMissingConditionalSelector(vertex, selector);
                    }
                }
                if (matched) {
                    logger.debug(String.format(SUCCEEDED_MATCHING_MESSAGE, vertex));
                    matchedVertices.add(vertex);
                }
            }
            if (!matchedVertices.isEmpty()) {
                results.add(new DSLResult(transposeFlowGraph, matchedVertices, constraintTrace));
            } else {
                logger.debug(String.format(OMMITED_TRANSPOSE_FLOW_GRAPH, transposeFlowGraph));
            }
        }
        return results;
    }

    public AbstractSelector getDataSources() {
        return dataSources;
    }

    public void setDataSources(AbstractSelector dataSources) {
        this.dataSources = dataSources;
    }

    public AbstractSelector getVertexDestinations() {
        return vertexDestinations;
    }

    public void setVertexDestinations(AbstractSelector vertexDestinations) {
        this.vertexDestinations = vertexDestinations;
    }

    /**
     * Adds a conditional selector to the query
     * @param selector Conditional selector that is added to the query
     */
    public void addConditionalSelector(ConditionalSelector selector) {
        this.selectors.add(selector);
    }

    /**
     * Returns the context of constraint variables of the query
     * @return Constraint variable context of the query
     */
    public DSLContext getContext() {
        return context;
    }
}
