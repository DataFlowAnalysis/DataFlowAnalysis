package org.dataflowanalysis.analysis.dfd.core;

import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;

public class DFDFlowGraph implements FlowGraph {
	private final List<AbstractPartialFlowGraph> partialFlowGraphs;
	private final DFDResourceProvider resourceProvider;
	
	public DFDFlowGraph(DFDResourceProvider resourceProvider) {
		this.resourceProvider = resourceProvider;
		this.partialFlowGraphs = this.findPartialFlowGraphs();
	}
	
	public DFDFlowGraph(DFDFlowGraph oldFlowGraph, List<AbstractPartialFlowGraph> partialFlowGraphs) {
		this.resourceProvider = oldFlowGraph.resourceProvider;
		this.partialFlowGraphs = partialFlowGraphs;
	}

	public List<AbstractPartialFlowGraph> findPartialFlowGraphs() {
		return DFDPartialFlowGraphFinder.findAllPartialFlowGraphsInDFD(this.resourceProvider.getDataFlowDiagram(), this.resourceProvider.getDataDictionary());
	}

	@Override
	public DFDFlowGraph evaluate() {
		List<AbstractPartialFlowGraph> evaluatedPartialFlowGraphs = new ArrayList<>();
		for (var dfdActionSequence : this.getPartialFlowGraphs()) {
			evaluatedPartialFlowGraphs.add(DFDCharacteristicsCalculator.fillDataFlowVariables((DFDPartialFlowGraph) dfdActionSequence));
		}
		return new DFDFlowGraph(this, evaluatedPartialFlowGraphs);
	}

	@Override
	public List<AbstractPartialFlowGraph> getPartialFlowGraphs() {
		return this.partialFlowGraphs;
	}
}
