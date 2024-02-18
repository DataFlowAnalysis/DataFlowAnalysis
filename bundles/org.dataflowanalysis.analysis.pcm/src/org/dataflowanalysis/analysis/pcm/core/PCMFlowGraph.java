package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.PartialFlowGraphFinder;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;
import org.dataflowanalysis.analysis.pcm.flowgraph.PCMPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;

public class PCMFlowGraph extends FlowGraph {
    private static final Logger logger = Logger.getLogger(PCMFlowGraph.class);

    public PCMFlowGraph(PCMResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public PCMFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs) {
        super(partialFlowGraphs);
    }

    public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
        if (!(super.resourceProvider instanceof PCMResourceProvider)) {
            logger.error("Cannot find partial flow graphs from non-pcm resource provider",
                    new IllegalArgumentException("Cannot find partial flow graphs with non-pcm resource provider"));
        }
        PCMResourceProvider pcmResourceProvider = (PCMResourceProvider) resourceProvider;
        PartialFlowGraphFinder sequenceFinder = new PCMActionSequenceFinder(pcmResourceProvider);

        return sequenceFinder.findPartialFlowGraphs().parallelStream().map(AbstractPartialFlowGraph.class::cast).collect(Collectors.toList());
    }

    @Override
    public PCMFlowGraph evaluate() {
        List<PCMPartialFlowGraph> actionSequences = this.getPartialFlowGraphs().parallelStream().map(PCMPartialFlowGraph.class::cast)
                .collect(Collectors.toList());
        List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = actionSequences.parallelStream().map(it -> it.evaluate()).toList();
        return new PCMFlowGraph(evaluatedPartialFlowGraphs);
    }
}
