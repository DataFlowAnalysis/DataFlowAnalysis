package org.dataflowanalysis.analysis.dsl;

import org.apache.commons.lang.NotImplementedException;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class AnalysisConstraint {
    private List<AbstractSelector> flowSource;
    private List<AbstractSelector> flowDestination;

    // TODO: Can we look at partial flow graphs independently?
    // TODO: This is a native implementation that does not account for variables
    public List<AbstractVertex<?>> matchPartialFlowGraph(AbstractPartialFlowGraph partialFlowGraph) {
        List<AbstractVertex<?>> results = new ArrayList<>();
        for (AbstractVertex<?> vertex : partialFlowGraph.getVertices()) {
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
}
