package org.dataflowanalysis.analysis.dfd.core;

import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposedFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;

/**
 * This class represents a flow graph in a dfd model
 */
public class DFDFlowGraphCollection extends FlowGraphCollection {
    private final Logger logger = Logger.getLogger(DFDFlowGraphCollection.class);

    /**
     * Creates a new instance of a dfd flow graph with the given resource provider. Transposed flow graphs are determined via
     * {@link DFDFlowGraphCollection#findTransposedFlowGraphs()}
     * @param resourceProvider Resource provider that provides model files to the transposed flow graph finder
     */
    public DFDFlowGraphCollection(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    /**
     * Find a list of transposed flow graphs that are contained in the model provided by the given resource provider
     * @return Returns a list of (unevaluated) transposed flow graphs
     */
    public List<AbstractTransposedFlowGraph> findTransposedFlowGraphs() {
        if (!(this.resourceProvider instanceof DFDResourceProvider dfdResourceProvider)) {
            logger.error("Cannot find transposed flow graphs for non-dfd resource provider");
            throw new IllegalArgumentException();
        }
        return new DFDTransposedFlowGraphFinder(dfdResourceProvider).findTransposedFlowGraphs();
    }
}
