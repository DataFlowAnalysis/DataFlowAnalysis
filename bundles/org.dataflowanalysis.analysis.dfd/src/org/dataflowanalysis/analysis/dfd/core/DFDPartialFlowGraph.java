package org.dataflowanalysis.analysis.dfd.core;

import java.util.HashSet;

import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;

public class DFDPartialFlowGraph extends AbstractPartialFlowGraph implements Comparable<DFDPartialFlowGraph> {


	
	public DFDPartialFlowGraph(AbstractVertex<?> sink) {
		super(sink);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(DFDPartialFlowGraph o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AbstractPartialFlowGraph evaluate() {
		DFDVertex newSink = ((DFDVertex)sink).clone();
		newSink.unify(new HashSet<>());
		newSink.evaluateDataFlow();
		return new DFDPartialFlowGraph(newSink);
	}

	
}
