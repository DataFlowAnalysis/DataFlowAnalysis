package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dsl.context.DSLContext;
import org.dataflowanalysis.analysis.dsl.result.DSLConstraintTrace;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an analysis query created by the DSL
 */
public class AnalysisQuery {
    private final List<AbstractSelector> flowSource;
    private final List<ConditionalSelector> selectors;
    private final DSLContext context;

    /**
     * Create a new empty analysis query
     */
    public AnalysisQuery() {
        this.flowSource = new ArrayList<>();
        this.selectors = new ArrayList<>();
        this.context = new DSLContext();
    }


    /**
     * Find queried vertices of the query in the given flow graph collection
     * @param flowGraphCollection Given flow graph collection in which the query is evaluated
     * @return Returns a list of dsl results for each <b>queried</b> transpose flow graph
     */
    public List<DSLResult> query(FlowGraphCollection flowGraphCollection) {
        List<DSLResult> results = new ArrayList<>();
        for(AbstractTransposeFlowGraph transposeFlowGraph : flowGraphCollection.getTransposeFlowGraphs()) {
            DSLConstraintTrace constraintTrace = new DSLConstraintTrace();
            List<AbstractVertex<?>> violations = new ArrayList<>();
            for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
                boolean matched = true;
                for (AbstractSelector selector : this.flowSource) {
                    if (!selector.matches(vertex)) {
                        matched = false;
                        constraintTrace.addMissingSelector(vertex, selector);
                    }
                }
                for(ConditionalSelector selector : this.selectors) {
                    if(!selector.matchesSelector(vertex, context)) {
                        matched = false;
                        constraintTrace.addMissingConditionalSelector(vertex, selector);
                    }
                }
                if (matched) {
                    violations.add(vertex);
                }
            }
            if (!violations.isEmpty()) {
                results.add(new DSLResult(transposeFlowGraph, violations, constraintTrace));
            }
        }
        return results;
    }

    /**
     * Adds a flow source selector to the query
     * @param selector Flow source selector that is added to the query
     */
    public void addFlowSource(AbstractSelector selector) {
        this.flowSource.add(selector);
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
