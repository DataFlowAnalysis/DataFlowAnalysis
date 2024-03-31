package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposedFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMTransposedFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public class PCMFlowGraph extends FlowGraph {
    private static final Logger logger = Logger.getLogger(PCMFlowGraph.class);

    public PCMFlowGraph(PCMResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public PCMFlowGraph(List<AbstractTransposedFlowGraph> transposedFlowGraphs, ResourceProvider resourceProvider) {
        super(transposedFlowGraphs, resourceProvider);
    }

    public List<AbstractTransposedFlowGraph> findTransposedFlowGraphs() {
        if (!(this.resourceProvider instanceof PCMResourceProvider pcmResourceProvider)) {
            logger.error("Cannot find transposed flow graphs from non-pcm resource provider");
            throw new IllegalArgumentException("Cannot find transposed flow graphs with non-pcm resource provider");
        }
        PCMTransposedFlowGraphFinder sequenceFinder = new PCMTransposedFlowGraphFinder(pcmResourceProvider);
        return sequenceFinder.findTransposedFlowGraphs().parallelStream().map(AbstractTransposedFlowGraph.class::cast).collect(Collectors.toList());
    }
}
