package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public class PCMFlowGraph extends FlowGraph {
    private static final Logger logger = Logger.getLogger(PCMFlowGraph.class);

    public PCMFlowGraph(PCMResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public PCMFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs, ResourceProvider resourceProvider) {
        super(partialFlowGraphs, resourceProvider);
    }

    public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
        if (!(this.resourceProvider instanceof PCMResourceProvider pcmResourceProvider)) {
            logger.error("Cannot find partial flow graphs from non-pcm resource provider");
            throw new IllegalArgumentException("Cannot find partial flow graphs with non-pcm resource provider");
        }
        PCMPartialFlowGraphFinder sequenceFinder = new PCMPartialFlowGraphFinder(pcmResourceProvider);
        return sequenceFinder.findPartialFlowGraphs().parallelStream().map(AbstractPartialFlowGraph.class::cast).collect(Collectors.toList());
    }

    @Override
    public PCMFlowGraph evaluate() {
        List<PCMPartialFlowGraph> actionSequences = this.getPartialFlowGraphs().parallelStream().map(PCMPartialFlowGraph.class::cast).toList();
        List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = actionSequences.parallelStream().map(PCMPartialFlowGraph::evaluate).toList();
        return new PCMFlowGraph(evaluatedPartialFlowGraphs, this.resourceProvider);
    }
}
