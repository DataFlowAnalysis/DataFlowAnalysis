package org.dataflowanalysis.analysis.dfd.core;

import java.util.List;
import java.util.ArrayList;

import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.VertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;

public class DFDPartialFlowGraph extends AbstractPartialFlowGraph implements Comparable<DFDPartialFlowGraph> {

private DFDVertex lastVertex;
	
	public DFDPartialFlowGraph(AbstractVertex<?> sink) {
		super(sink);
		// TODO Auto-generated constructor stub
	}
	
	private static List<AbstractVertex<?>> buildFlowGraphRecFromEnd(DFDVertex endVertex) {
		List<AbstractVertex<?>> sequenceAsList = new ArrayList<>();
		for (var prevElement : endVertex.getMapPinToPreviousVertex().values()) {
			sequenceAsList.addAll(buildFlowGraphRecFromEnd(prevElement));
		}
		sequenceAsList.add(endVertex);
		return sequenceAsList;
	}

	@Override
	public int compareTo(DFDPartialFlowGraph o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public AbstractPartialFlowGraph evaluate() {
		// TODO Auto-generated method stub
		return null;
	}

	public DFDVertex getLastVertex() {
		return lastVertex;
	}

	public void setLastVertex(DFDVertex lastElement) {
		this.lastVertex = lastElement;
	}

	
}
