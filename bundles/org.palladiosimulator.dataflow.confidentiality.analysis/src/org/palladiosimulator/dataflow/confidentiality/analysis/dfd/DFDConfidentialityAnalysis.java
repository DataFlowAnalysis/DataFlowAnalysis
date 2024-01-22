package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.DFDCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDFlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.dfd.DFDVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractVertex;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.dfd.DFDFlowGraphFinder;

import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;


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
	public List<FlowGraph> findAllFlowGraphs() {
		return DFDFlowGraphFinder.findAllFlowGraphsInDFD(dfd, dataDictionary);
	}
	

	@Override
	public List<FlowGraph> evaluateDataFlows(List<FlowGraph> sequences) {
		List<FlowGraph> outSequences = new ArrayList<>();
		for (var dfdActionSequence : sequences) {
			outSequences.add(DFDCharacteristicsCalculator.fillDataFlowVariables((DFDFlowGraph)dfdActionSequence));
		}
		return outSequences;
	}

	@Override
	public List<AbstractVertex<?>> queryDataFlow(FlowGraph sequence,
			Predicate<? super AbstractVertex<?>> condition) {
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
