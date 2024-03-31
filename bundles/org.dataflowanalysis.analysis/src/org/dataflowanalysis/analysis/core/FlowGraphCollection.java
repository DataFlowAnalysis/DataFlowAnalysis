package org.dataflowanalysis.analysis.core;

import java.util.List;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents an abstract flow graph collection that contains all flows contained in a model. The method
 * {@link FlowGraphCollection#findTransposedFlowGraphs()} will be called to determine the transposed flow graphs for the specific
 * implementation of the flow graph
 */
public abstract class FlowGraphCollection {
    protected final ResourceProvider resourceProvider;
    private List<? extends AbstractTransposedFlowGraph> transposedFlowGraphs;

    /**
     * Creates a new collection of flow graphs with the given resource provider. Furthermore, the list of transposed flow graphs is determined
     * by calling {@link FlowGraphCollection#findTransposedFlowGraphs()}
     * @param resourceProvider Resource provider, that provides model files to the transposed flow graph finder
     */
    public FlowGraphCollection(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.transposedFlowGraphs = this.findTransposedFlowGraphs();
    }

    /**
     * Initializes a new collection of flow graphs with the given transposed flow graphs
     * @param transposedFlowGraphs List of transposed flow graphs that are contained in the flow graph
     * @param resourceProvider Resource provider that provides model files to the transposed flow graph finder
     */
    public FlowGraphCollection(List<? extends AbstractTransposedFlowGraph> transposedFlowGraphs, ResourceProvider resourceProvider) {
        this.transposedFlowGraphs = transposedFlowGraphs;
        this.resourceProvider = resourceProvider;
    }

    /**
     * Determines the transposed flow graphs present in the model pointed to by {@link FlowGraphCollection#resourceProvider}
     * @return Returns a list of (unevaluated) transposed flow graphs contained in the model
     */
    public abstract List<AbstractTransposedFlowGraph> findTransposedFlowGraphs();

    /**
     * Evaluates the collection of flow graphs with label propagation. An evaluated copy of the flow graph is returned by this method
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
