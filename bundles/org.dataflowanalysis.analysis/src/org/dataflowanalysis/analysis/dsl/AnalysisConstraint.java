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
    private final Logger logger = Logger.getLogger(AnalysisConstraint.class);
    private final List<AbstractSelector> flowSource;
    private final List<AbstractSelector> flowDestination;
    private final List<ConditionalSelector> selectors;
    private final DSLContext context;

    /**
     * Create a new analysis constraint with no constraints
     */
    public AnalysisConstraint() {
        this.flowSource = new ArrayList<>();
        this.flowDestination = new ArrayList<>();
        this.selectors = new ArrayList<>();
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
                for (AbstractSelector selector : Stream.concat(flowSource.stream(), flowDestination.stream()).toList()) {
                    if (!selector.matches(vertex)) {
                        matched = false;
                        constraintTrace.addMissingSelector(vertex, selector);
                    }
                }
                for (ConditionalSelector selector : selectors) {
                    if (!selector.matchesSelector(vertex, context)) {
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
     * Adds a flow source selector to the constraint
     * @param selector Flow source selector that is added to the constraint
     */
    public void addFlowSource(AbstractSelector selector) {
        this.flowSource.add(selector);
    }

    /**
     * Adds a flow destination selector to the constraint
     * @param selector Flow destination selector that is added to the constraint
     */
    public void addFlowDestination(AbstractSelector selector) {
        this.flowDestination.add(selector);
    }

    /**
     * Adds a conditional selector to the constraint
     * @param selector Conditional selector that is added to the constraint
     */
    public void addConditionalSelector(ConditionalSelector selector) {
        this.selectors.add(selector);
    }

    /**
     * Returns the context of constraint variables of the constraint
     * @return Constraint variable context of the constraint
     */
    public DSLContext getContext() {
        return context;
    }
}
