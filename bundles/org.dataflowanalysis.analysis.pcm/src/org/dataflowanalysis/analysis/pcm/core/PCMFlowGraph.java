package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public class PCMFlowGraph extends FlowGraph {
    private final PCMPartialFlowGraphFinder sequenceFinder;

    public PCMFlowGraph(PCMResourceProvider resourceProvider) {
        this(resourceProvider, new PCMPartialFlowGraphFinder(resourceProvider));
    }

    public PCMFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs, ResourceProvider resourceProvider) {
        this(partialFlowGraphs, resourceProvider, new PCMPartialFlowGraphFinder(resourceProvider));
    }
    
    public PCMFlowGraph(PCMResourceProvider resourceProvider, PCMPartialFlowGraphFinder finder) {
    	this.sequenceFinder = finder;
    	initialize(resourceProvider);
    }
    
    public PCMFlowGraph(List<AbstractPartialFlowGraph> partialFlowGraphs, ResourceProvider resourceProvider, 
    		PCMPartialFlowGraphFinder finder) {
    	super(partialFlowGraphs, resourceProvider);
    	this.sequenceFinder = finder;
    }

    public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
        return sequenceFinder.findPartialFlowGraphs().parallelStream().map(AbstractPartialFlowGraph.class::cast).collect(Collectors.toList());
    }

    @Override
    public PCMFlowGraph evaluate() {
        List<PCMPartialFlowGraph> actionSequences = this.getPartialFlowGraphs().parallelStream().map(PCMPartialFlowGraph.class::cast).toList();
        List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = actionSequences.parallelStream().map(PCMPartialFlowGraph::evaluate).toList();
        return new PCMFlowGraph(evaluatedPartialFlowGraphs, getResourceProvider(), this.sequenceFinder);
    }
}
