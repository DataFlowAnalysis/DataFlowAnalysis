package org.dataflowanalysis.analysis.core;

import java.util.List;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents an abstract flow graph that contains all flows contained in a model. The method
 * {@link FlowGraph#findTransposedFlowGraphs()} will be called to determine the transposed flow graphs for the specific
 * implementation of the flow graph
 */
public abstract class FlowGraph {
    protected final ResourceProvider resourceProvider;
    private List<? extends AbstractTransposedFlowGraph> transposedFlowGraphs;

    /**
     * Creates a new flow graph with the given resource provider. Furthermore, the list of transposed flow graphs is determined
     * by calling {@link FlowGraph#findTransposedFlowGraphs()}
     * @param resourceProvider Resource provider, that provides model files to the transposed flow graph finder
     */
    public FlowGraph(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.transposedFlowGraphs = this.findTransposedFlowGraphs();
    }

    /**
     * Initializes a new flow graph with the given transposed flow graphs
     * @param transposedFlowGraphs List of transposed flow graphs that are contained in the flow graph
     * @param resourceProvider Resource provider that provides model files to the transposed flow graph finder
     */
    public FlowGraph(List<? extends AbstractTransposedFlowGraph> transposedFlowGraphs, ResourceProvider resourceProvider) {
        this.transposedFlowGraphs = transposedFlowGraphs;
        this.resourceProvider = resourceProvider;
    }

    /**
     * Determines the transposed flow graphs present in the model pointed to by {@link FlowGraph#resourceProvider}
     * @return Returns a list of (unevaluated) transposed flow graphs contained in the model
     */
    public abstract List<AbstractTransposedFlowGraph> findTransposedFlowGraphs();

    /**
     * Evaluates the flow graph by label propagation. An evaluated copy of the flow graph is returned by this method
     */
    public void evaluate() {
        this.transposedFlowGraphs = this.getTransposedFlowGraphs().stream()
                .map(AbstractTransposedFlowGraph::evaluate)
                .toList();
    }

    /**
     * Returns the list of saved transposed flow graphs that are contained in the flow graph
     * @return Returns a list of saved transposed flow graphs
     */
    public List<? extends AbstractTransposedFlowGraph> getTransposedFlowGraphs() {
        return this.transposedFlowGraphs;
    }
}
