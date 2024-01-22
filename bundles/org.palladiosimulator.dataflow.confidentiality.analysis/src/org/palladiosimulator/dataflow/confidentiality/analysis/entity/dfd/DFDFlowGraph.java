package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

public class DFDFlowGraph extends FlowGraph implements Comparable<DFDFlowGraph> {
	
	private DFDVertex lastVertex;
	
	public DFDFlowGraph(List<AbstractVertex<?>> elements) {
		super(elements);
		// TODO Auto-generated constructor stub
	}
	
	public static DFDFlowGraph createFromEndVertex(DFDVertex endVertex) {
		DFDFlowGraph sequence = new DFDFlowGraph(buildFlowGraphRecFromEnd(endVertex));
		sequence.setLastVertex(endVertex);
		return sequence;
		// TODO Auto-generated constructor stub
	}
	
	private static List<AbstractVertex<?>> buildFlowGraphRecFromEnd(DFDVertex endVortex) {
		List<AbstractVertex<?>> sequenceAsList = new ArrayList<>();
		for (var prevElement : endVortex.getMapPinToPreviousVertex().values()) {
			sequenceAsList.addAll(buildFlowGraphRecFromEnd(prevElement));
		}
		sequenceAsList.add(endVortex);
		return sequenceAsList;
	}

	@Override
	public int compareTo(DFDFlowGraph o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FlowGraph evaluateDataFlow(AnalysisData analysisData) {
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
