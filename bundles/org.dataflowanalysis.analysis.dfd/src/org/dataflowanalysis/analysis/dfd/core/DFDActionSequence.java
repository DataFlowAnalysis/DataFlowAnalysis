package org.dataflowanalysis.analysis.dfd.core;

import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.NodeCharacteristicsCalculator;

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
	public ActionSequence evaluateDataFlow(NodeCharacteristicsCalculator nodeCharacteristicsCalculator, DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
		// TODO Auto-generated method stub
		return null;
	}

}
