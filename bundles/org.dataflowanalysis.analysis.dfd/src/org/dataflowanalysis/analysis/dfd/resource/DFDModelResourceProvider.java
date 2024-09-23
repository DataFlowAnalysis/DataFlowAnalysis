package org.dataflowanalysis.analysis.dfd.resource;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

public class DFDModelResourceProvider extends DFDResourceProvider{
	
	DataDictionary dataDictionary;
	DataFlowDiagram dataFlowDiagram;
	
	public DFDModelResourceProvider(DataDictionary dataDictionary, DataFlowDiagram dataFlowDiagram) {
		this.dataDictionary = dataDictionary;
		this.dataFlowDiagram = dataFlowDiagram;
	}

	@Override
	public DataFlowDiagram getDataFlowDiagram() {
		return this.dataFlowDiagram;
	}

	@Override
	public DataDictionary getDataDictionary() {
		return this.dataDictionary;
	}

	@Override
	public void loadRequiredResources() {
		return;
	}

	
}
