package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

public class DFDFlowGraph extends FlowGraph implements Comparable<DFDFlowGraph> {
	
	private DFDVertex lastElement;
	
	public DFDFlowGraph(List<AbstractActionSequenceElement<?>> elements) {
		super(elements);
		// TODO Auto-generated constructor stub
	}
	
	public static DFDFlowGraph createFromEndElement(DFDVertex endElement) {
		DFDFlowGraph sequence = new DFDFlowGraph(buildFlowGraphRecFromEnd(endElement));
		sequence.setLastElement(endElement);
		return sequence;
		// TODO Auto-generated constructor stub
	}
	
	private static List<AbstractActionSequenceElement<?>> buildFlowGraphRecFromEnd(DFDVertex endElement) {
		List<AbstractActionSequenceElement<?>> sequenceAsList = new ArrayList<>();
		for (var prevElement : endElement.getMapPinToPreviousElement().values()) {
			sequenceAsList.addAll(buildFlowGraphRecFromEnd(prevElement));
		}
		sequenceAsList.add(endElement);
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

	public DFDVertex getLastElement() {
		return lastElement;
	}

	public void setLastElement(DFDVertex lastElement) {
		this.lastElement = lastElement;
	}
	
	

}
