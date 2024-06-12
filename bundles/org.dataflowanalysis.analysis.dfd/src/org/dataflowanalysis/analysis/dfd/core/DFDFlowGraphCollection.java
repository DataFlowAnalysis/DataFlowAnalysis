package org.dataflowanalysis.analysis.dfd.core;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents a flow graph in a dfd model
 */
public class DFDFlowGraphCollection extends FlowGraphCollection {
    private final Logger logger = Logger.getLogger(DFDFlowGraphCollection.class);

    /**
     * Creates a new collection of flow graphs.
     * {@link DFDFlowGraphCollection#initialize(ResourceProvider)} should be called before this class is used
     */
    public DFDFlowGraphCollection() {
        super();
    }

    /**
     * Initializes the flow graph collection with the given resource provider
     * @param resourceProvider Resource provider used to find transpose flow graphs
     */
    public void initialize(ResourceProvider resourceProvider) {
        super.initialize(resourceProvider);
    }

    /**
     * Creates a new instance of a dfd flow graph with the given resource provider. Transpose flow graphs are determined via
     * {@link DFDFlowGraphCollection#findTransposeFlowGraphs()}
     * @param resourceProvider Resource provider that provides model files to the transpose flow graph finder
     */
    public DFDFlowGraphCollection(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    /**
     * Creates a new instance of a dfd flow graph with the given resource provider and list of transpose flow graphs. Transpose flow graphs are determined via
     * {@link DFDFlowGraphCollection#findTransposeFlowGraphs()}
     * @param resourceProvider Resource provider that provides model files to the transpose flow graph finder
     * @param transposeFlowGraphs Transpose flow graphs saved in the flow graph collection
     */
    public DFDFlowGraphCollection(DFDResourceProvider resourceProvider, List<? extends AbstractTransposeFlowGraph> transposeFlowGraphs) {
        super(transposeFlowGraphs, resourceProvider);
    }

    /**
     * Find a list of transpose flow graphs that are contained in the model provided by the given resource provider
     * @return Returns a list of (unevaluated) transpose flow graphs
     */
    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        if (!(this.resourceProvider instanceof DFDResourceProvider dfdResourceProvider)) {
            logger.error("Cannot find transpose flow graphs for non-dfd resource provider");
            throw new IllegalArgumentException();
        }
        return new DFDTransposeFlowGraphFinder(dfdResourceProvider).findTransposeFlowGraphs();
    }
}
