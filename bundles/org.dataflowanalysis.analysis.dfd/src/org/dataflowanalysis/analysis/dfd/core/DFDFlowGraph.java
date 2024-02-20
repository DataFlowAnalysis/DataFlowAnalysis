package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

/**
 * This class represents a flow graph in a dfd model
 */
public class DFDFlowGraph extends FlowGraph {
    private final Logger logger = Logger.getLogger(DFDFlowGraph.class);

    /**
     * Creates a new instance of a dfd flow graph with the given resource provider. Partial flow graphs are determined via
     * {@link DFDFlowGraph#findPartialFlowGraphs()}
     * @param resourceProvider Resource provider that provides model files to the partial flow graph finder
     */
    public DFDFlowGraph(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    /**
     * Creates a new instance of a dfd flow graph with the given partial flow graphs and resource provider
     * @param partialFlowGraphs Partial flow graphs the dfd flow graph should contain
     * @param resourceProvider Resource provider that provides model files
     */
    public DFDFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs, ResourceProvider resourceProvider) {
        super(partialFlowGraphs, resourceProvider);
    }

    /**
     * Find a list of partial flow graphs that are contained in the model provided by the given resource provider
     * @return Returns a list of (unevaluated) partial flow graphs
     */
    public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
        if (!(this.resourceProvider instanceof DFDResourceProvider dfdResourceProvider)) {
            logger.error("Cannot find partial flow graphs for non-dfd resource provider");
            throw new IllegalArgumentException();
        }
        return DFDPartialFlowGraphFinder.findAllPartialFlowGraphsInDFD(dfdResourceProvider.getDataFlowDiagram(),
                dfdResourceProvider.getDataDictionary());
    }

    /**
     * Evaluates the given flow graph by evaluating all contained partial flow graphs. The result of the evaluation is
     * returned in a new dfd flow graph
     * @return New dfd flow graph containing the evaluated partial flow graphs
     */
    @Override
    public DFDFlowGraph evaluate() {
        List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = new ArrayList<>();
        for (var dfdPartialFlowGraph : this.getPartialFlowGraphs()) {
            evaluatedPartialFlowGraphs.add(dfdPartialFlowGraph.evaluate());
        }
        return new DFDFlowGraph(evaluatedPartialFlowGraphs, this.resourceProvider);
    }
}
