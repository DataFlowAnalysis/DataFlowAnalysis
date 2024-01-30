package org.dataflowanalysis.analysis.pcm.core;

import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.VertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;
import org.dataflowanalysis.analysis.core.PartialFlowGraphFinder;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;

public class PCMFlowGraph implements FlowGraph {
	private final List<AbstractPartialFlowGraph> partialFlowGraphs;
	private final PCMResourceProvider resourceProvider;
	private final VertexCharacteristicsCalculator nodeCharacteristicsCalculator;
	private final DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory;
	
	public PCMFlowGraph(PCMResourceProvider resourceProvider, VertexCharacteristicsCalculator nodeCharacteristicsCalculator, 
			DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
		this.resourceProvider = resourceProvider;
		this.nodeCharacteristicsCalculator = nodeCharacteristicsCalculator;
		this.dataCharacteristicsCalculatorFactory = dataCharacteristicsCalculatorFactory;
		this.partialFlowGraphs = this.findPartialFlowGraphs();
	}
	
	public PCMFlowGraph(PCMFlowGraph oldFlowGraph, List<AbstractPartialFlowGraph> partialFlowGraphs) {
		this.partialFlowGraphs = partialFlowGraphs;
		this.resourceProvider = oldFlowGraph.resourceProvider;
		this.nodeCharacteristicsCalculator = oldFlowGraph.nodeCharacteristicsCalculator;
		this.dataCharacteristicsCalculatorFactory = oldFlowGraph.dataCharacteristicsCalculatorFactory;
	}

	public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
		PartialFlowGraphFinder sequenceFinder = new PCMActionSequenceFinder(resourceProvider.getUsageModel());
		
		return sequenceFinder.findPartialFlowGraphs().parallelStream()
			.map(AbstractPartialFlowGraph.class::cast)
			.collect(Collectors.toList());
	}

	@Override
	public PCMFlowGraph evaluate() {
		List<PCMPartialFlowGraph> actionSequences = this.getPartialFlowGraphs()
				.parallelStream()
    			.map(PCMPartialFlowGraph.class::cast)
    			.collect(Collectors.toList());
    	List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = 
    			actionSequences.parallelStream()
    	          .map(it -> it.evaluate(this.nodeCharacteristicsCalculator, this.dataCharacteristicsCalculatorFactory))
    	          .toList();
		return new PCMFlowGraph(this, evaluatedPartialFlowGraphs);
	}

	@Override
	public List<AbstractPartialFlowGraph> getPartialFlowGraphs() {
		return this.partialFlowGraphs;
	}

}
