package org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

public class DFDActionSequence extends ActionSequence implements Comparable<DFDActionSequence> {

	public DFDActionSequence(List<AbstractActionSequenceElement<?>> elements) {
		super(elements);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(DFDActionSequence o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ActionSequence evaluateDataFlow(AnalysisData analysisData) {
		// TODO Auto-generated method stub
		return null;
	}

}
