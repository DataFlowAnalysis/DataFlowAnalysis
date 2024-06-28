package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.core.finder.PCMTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public class PCMFlowGraphCollection extends FlowGraphCollection {
    private static final Logger logger = Logger.getLogger(PCMFlowGraphCollection.class);

    public PCMFlowGraphCollection() {

    }

    @Override
    public void initialize(ResourceProvider resourceProvider) {
        super.initialize(resourceProvider);
    }

    public PCMFlowGraphCollection(PCMResourceProvider resourceProvider) {
        super(resourceProvider);
    }

    public PCMFlowGraphCollection(List<? extends AbstractTransposeFlowGraph> transposeFlowGraphs, ResourceProvider resourceProvider) {
        super(transposeFlowGraphs, resourceProvider);
    }

    public List<? extends AbstractTransposeFlowGraph> findTransposeFlowGraphs() {
        if (!(this.resourceProvider instanceof PCMResourceProvider pcmResourceProvider)) {
            logger.error("Cannot find transpose flow graphs from non-pcm resource provider");
            throw new IllegalArgumentException("Cannot find transpose flow graphs with non-pcm resource provider");
        }
        PCMTransposeFlowGraphFinder sequenceFinder = new PCMTransposeFlowGraphFinder(pcmResourceProvider);
        return sequenceFinder.findTransposeFlowGraphs()
                .parallelStream()
                .map(AbstractTransposeFlowGraph.class::cast)
                .collect(Collectors.toList());
    }
}
