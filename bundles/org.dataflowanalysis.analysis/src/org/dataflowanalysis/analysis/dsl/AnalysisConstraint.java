package org.dataflowanalysis.analysis.dsl;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;

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

    public List<AbstractVertex<?>> matchPartialFlowGraph(AbstractTransposeFlowGraph transposeFlowGraph) {
        List<AbstractVertex<?>> results = new ArrayList<>();
        for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
            if (Stream.concat(flowSource.stream(), flowDestination.stream())
                    .allMatch(it -> it.matches(vertex))) {
                if (selectors.stream().allMatch(it -> it.matchesSelector(vertex, context))) {
                    results.add(vertex);
                }
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
