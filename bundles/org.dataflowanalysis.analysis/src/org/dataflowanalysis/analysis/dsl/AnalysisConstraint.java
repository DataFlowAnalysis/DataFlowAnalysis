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

public class AnalysisConstraint {
    private final Logger logger = Logger.getLogger(AnalysisConstraint.class);
    private final List<AbstractSelector> flowSource;
    private final List<AbstractSelector> flowDestination;
    private final List<ConditionalSelector> selectors;
    private final DSLContext context;

    public AnalysisConstraint() {
        this.flowSource = new ArrayList<>();
        this.flowDestination = new ArrayList<>();
        this.selectors = new ArrayList<>();
        this.context = new DSLContext();
    }

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

    public void addFlowSource(AbstractSelector selector) {
        this.flowSource.add(selector);
    }

    public void addFlowDestination(AbstractSelector selector) {
        this.flowDestination.add(selector);
    }

    public void addConditionalSelector(ConditionalSelector selector) {
        this.selectors.add(selector);
    }

    public DSLContext getContext() {
        return context;
    }
}
