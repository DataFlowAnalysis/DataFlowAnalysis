package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;

public class DFDFlowGraph extends FlowGraph {
    private final Logger logger = Logger.getLogger(DFDFlowGraph.class);

    public DFDFlowGraph(DFDResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public DFDFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs) {
        super(partialFlowGraphs);
    }

    public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
        if (!(this.resourceProvider instanceof DFDResourceProvider)) {
            logger.error("Cannot find partial flow graphs for non-dfd resource provider", new IllegalArgumentException());
        }
        DFDResourceProvider dfdResourceProvider = (DFDResourceProvider) this.resourceProvider;
        return DFDPartialFlowGraphFinder.findAllPartialFlowGraphsInDFD(dfdResourceProvider.getDataFlowDiagram(),
                dfdResourceProvider.getDataDictionary());
    }

    @Override
    public DFDFlowGraph evaluate() {
        List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = new ArrayList<>();
        for (var dfdPartialFlowGraph : this.getPartialFlowGraphs()) {
            evaluatedPartialFlowGraphs.add(dfdPartialFlowGraph.evaluate());
        }
        return new DFDFlowGraph(evaluatedPartialFlowGraphs);
    }
}
