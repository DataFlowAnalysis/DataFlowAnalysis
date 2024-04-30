package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AnalysisConstraint {
    private final List<AbstractSelector> flowSource;
    private final List<AbstractSelector> flowDestination;
    private final List<ConditionalSelector> selectors;

    public AnalysisConstraint() {
        this.flowSource = new ArrayList<>();
        this.flowDestination = new ArrayList<>();
        this.selectors = new ArrayList<>();
    }

    // TODO: Can we look at partial flow graphs independently?
    // TODO: This is a native implementation that does not account for variables
    public List<AbstractVertex<?>> matchPartialFlowGraph(AbstractTransposeFlowGraph transposeFlowGraph) {
        List<AbstractVertex<?>> results = new ArrayList<>();
        for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
            if (Stream.concat(flowSource.parallelStream(), flowDestination.parallelStream())
                    .allMatch(it -> it.matches(vertex))) {
                results.add(vertex);
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
}
