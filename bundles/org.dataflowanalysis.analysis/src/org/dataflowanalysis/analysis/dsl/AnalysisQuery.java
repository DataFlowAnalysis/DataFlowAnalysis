package org.dataflowanalysis.analysis.dsl;

import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.dsl.selectors.AbstractSelector;
import org.dataflowanalysis.analysis.dsl.selectors.ConditionalSelector;

import java.util.ArrayList;
import java.util.List;

public class AnalysisQuery {
    private final List<AbstractSelector> flowSource;
    private final List<ConditionalSelector> selectors;
    private final DSLContext context;

    public AnalysisQuery() {
        this.flowSource = new ArrayList<>();
        this.selectors = new ArrayList<>();
        this.context = new DSLContext();
    }


    // TODO: Robust implementation of variables and evaluation; Return more user-friendly result
    public List<AbstractVertex<?>> matchPartialFlowGraph(AbstractTransposeFlowGraph transposeFlowGraph) {
        List<AbstractVertex<?>> results = new ArrayList<>();
        for (AbstractVertex<?> vertex : transposeFlowGraph.getVertices()) {
            if (flowSource.parallelStream().allMatch(it -> it.matches(vertex))) {
                results.add(vertex);
            }
        }
        return results;
    }

    public void addFlowSource(AbstractSelector selector) {
        this.flowSource.add(selector);
    }

    public void addConditionalSelector(ConditionalSelector selector) {
        this.selectors.add(selector);
    }

    public DSLContext getContext() {
        return context;
    }
}
