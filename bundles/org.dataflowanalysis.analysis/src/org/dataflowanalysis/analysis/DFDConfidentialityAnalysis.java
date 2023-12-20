package org.dataflowanalysis.analysis;


import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.core.dfd.DFDActionSequence;
import org.dataflowanalysis.analysis.core.dfd.DFDActionSequenceFinder;
import org.dataflowanalysis.analysis.core.dfd.DFDCharacteristicsCalculator;
import org.dataflowanalysis.analysis.resource.DFDLoader;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;


public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	DataFlowDiagram dfd;
	DataDictionary dd;
	
	public DFDConfidentialityAnalysis(DataFlowDiagram dfd, DataDictionary dd) {
		this.dfd = dfd;
		this.dd = dd;
	}

	@Override
	public boolean initializeAnalysis() {
		return true;
	}
	

	@Override
	public List<ActionSequence> findAllSequences() {
		return DFDActionSequenceFinder.findAllSequencesInDFD(dfd, dd);
	}
	

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		List<ActionSequence> outSequences = new ArrayList<>();
		for (var dfdActionSequence : sequences) {
			outSequences.add(DFDCharacteristicsCalculator.fillDataFlowVariables((DFDActionSequence)dfdActionSequence));
		}
		return outSequences;
	}

	@Override
	public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
			Predicate<? super AbstractActionSequenceElement<?>> condition) {
		return sequence.getElements()
	            .parallelStream()
	            .filter(condition)
	            .toList();
	}

	@Override
	public void setLoggerLevel(Level level) {
		// TODO Auto-generated method stub
		
	}
}