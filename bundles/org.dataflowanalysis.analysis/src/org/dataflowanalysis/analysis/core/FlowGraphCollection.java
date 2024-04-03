package org.dataflowanalysis.analysis.core;

import java.util.List;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents an abstract flow graph collection that contains all flows contained in a model. The method
 * {@link FlowGraphCollection#findTransposeFlowGraphs()} will be called to determine the transpose flow graphs for the
 * specific implementation of the flow graph
 */
public abstract class FlowGraphCollection {
    protected final ResourceProvider resourceProvider;
    private List<? extends AbstractTransposeFlowGraph> transposeFlowGraphs;

    /**
     * Creates a new collection of flow graphs with the given resource provider. Furthermore, the list of transpose flow
     * graphs is determined by calling {@link FlowGraphCollection#findTransposeFlowGraphs()}
     * @param resourceProvider Resource provider, that provides model files to the transpose flow graph finder
     */
    public FlowGraphCollection(ResourceProvider resourceProvider) {
        this.resourceProvider = resourceProvider;
        this.transposeFlowGraphs = this.findTransposeFlowGraphs();
    }

    /**
     * Initializes a new collection of flow graphs with the given transpose flow graphs
     * @param transposeFlowGraphs List of transpose flow graphs that are contained in the flow graph
     * @param resourceProvider Resource provider that provides model files to the transpose flow graph finder
     */
    public FlowGraphCollection(List<? extends AbstractTransposeFlowGraph> transposeFlowGraphs, ResourceProvider resourceProvider) {
        this.transposeFlowGraphs = transposeFlowGraphs;
        this.resourceProvider = resourceProvider;
    }

    /**
     * Determines the transpose flow graphs present in the model pointed to by {@link FlowGraphCollection#resourceProvider}
     * @return Returns a list of (unevaluated) transpose flow graphs contained in the model
     */
    public abstract List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs();

    /**
     * Evaluates the collection of flow graphs with label propagation. An evaluated copy of the flow graph is returned by
     * this method
     */
    public void evaluate() {
        this.transposeFlowGraphs = this.getTransposeFlowGraphs()
                .stream()
                .map(AbstractTransposeFlowGraph::evaluate)
                .toList();
    }

    /**
     * Returns the list of saved transpose flow graphs that are contained in the flow graph
     * @return Returns a list of saved transpose flow graphs
     */
    public List<? extends AbstractTransposeFlowGraph> getTransposeFlowGraphs() {
        return this.transposeFlowGraphs;
    }
}
