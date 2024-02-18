package org.dataflowanalysis.analysis.core;

import java.util.List;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents a abstract flow graph that contains all flows contained in a model. The method
 * {@link FlowGraph#findPartialFlowGraphs()} will be called to determine the partial flow graphs for the specific
 * implementation of the flow graph. TODO: Finding the partial flow graphs needs to happen at the _END_ of the
 * constructors, as {@link FlowGraph#findPartialFlowGraphs()} needs to use attributes from the subclass
 */
public abstract class FlowGraph {
    protected ResourceProvider resourceProvider;
    private List<? extends AbstractPartialFlowGraph> partialFlowGraphs;

    public FlowGraph(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.partialFlowGraphs = this.findPartialFlowGraphs();
    }

    public FlowGraph(List<? extends AbstractPartialFlowGraph> partialFlowGraphs) {
        this.partialFlowGraphs = partialFlowGraphs;
    }

    public abstract List<AbstractPartialFlowGraph> findPartialFlowGraphs();

    public abstract FlowGraph evaluate();

    public List<? extends AbstractPartialFlowGraph> getPartialFlowGraphs() {
        return this.partialFlowGraphs;
    }
}
