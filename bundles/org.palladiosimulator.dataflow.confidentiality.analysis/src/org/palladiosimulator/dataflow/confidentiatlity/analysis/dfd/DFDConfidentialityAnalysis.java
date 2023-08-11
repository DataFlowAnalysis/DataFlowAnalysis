package org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.DFDCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;

import mdpa.dfd.dataflowdiagram.DataFlowDiagram;
import mdpa.dfd.datadictionary.DataDictionary;


public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private String pathToDFDModel;
	private String pathToDataDictionaryModel;
	DataFlowDiagram dfd;
	DataDictionary dataDictionary;
	
	public DFDConfidentialityAnalysis(String pathToDFDModel, String pathToDataDictionaryModel) {
		this.pathToDFDModel = pathToDFDModel;
		this.pathToDataDictionaryModel = pathToDataDictionaryModel;
	}

	@Override
	public boolean initializeAnalysis() {
		this.dfd = DFDLoader.loadDFDModel(this.pathToDFDModel);
		this.dataDictionary = DFDLoader.loadDataDictionaryModel(this.pathToDataDictionaryModel);
		return true;
	}
	

	@Override
	public List<ActionSequence> findAllSequences() {
		return DFDMapper.findAllSequencesInDFD(dfd, dataDictionary);
	}
	

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		for (var dfdActionSequence : sequences) {
			for (var dfdActionSequenceElement : dfdActionSequence.getElements())
				dfdActionSequenceElement = DFDCharacteristicsCalculator.fillDataFlowVariables((DFDActionSequenceElement) dfdActionSequenceElement);
		}
		return sequences;
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
	
	public String getPathToDFDModel() {
		return this.pathToDFDModel;
	}
	
	public void setPathToDFDModel(String pathToModel) {
		this.pathToDFDModel = pathToModel;
	}
	
	public String getpathToDataDictionaryModel() {
		return this.pathToDataDictionaryModel;
	}
	
	public void setpathToDataDictionaryModel(String pathToModel) {
		this.pathToDataDictionaryModel = pathToModel;
	}


}
