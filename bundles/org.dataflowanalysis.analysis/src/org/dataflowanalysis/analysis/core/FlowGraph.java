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

    /**
     * Creates a new flow graph with the given resource provider.
     * Furthermore, the list of partial flow graphs is determined by calling {@link FlowGraph#findPartialFlowGraphs()}
     * @param resourceProvider Resource provider, that provides model files to the partial flow graph finder
     */
    public FlowGraph(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.partialFlowGraphs = this.findPartialFlowGraphs();
    }

    /**
     * Initializes a new flow graph with the given partial flow graphs
     * @param partialFlowGraphs List of partial flow graphs that are contained in the flow graph
     */
    public FlowGraph(List<? extends AbstractPartialFlowGraph> partialFlowGraphs) {
        this.partialFlowGraphs = partialFlowGraphs;
    }

    /**
     * Determines the partial flow graphs present in the model pointed to by {@link FlowGraph#resourceProvider}
     * @return Returns a list of (unevaluated) partial flow graphs contained in the model
     */
    public abstract List<AbstractPartialFlowGraph> findPartialFlowGraphs();

    /**
     * Evaluates the flow graph by label propagation. 
     * An evaluated copy of the flow graph is returned by this method
     * @return Returns a new flow graph with evaluated partial flow graphs
     */
    public abstract FlowGraph evaluate();

    /**
     * Returns the list of saved partial flow graphs that are contained in the flow graph
     * @return Returns a list of saved partial flow graphs
     */
    public List<? extends AbstractPartialFlowGraph> getPartialFlowGraphs() {
        return this.partialFlowGraphs;
    }
}