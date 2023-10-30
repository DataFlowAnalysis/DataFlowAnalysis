package org.dataflowanalysis.analysis.builder;

import org.dataflowanalysis.analysis.characteristics.node.NodeCharacteristicsCalculator;
import org.dataflowanalysis.analysis.characteristics.variable.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.resource.ResourceProvider;

public record AnalysisData(ResourceProvider resourceProvider, 
		NodeCharacteristicsCalculator nodeCharacteristicsCalculator,
		DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator) {
	
	public AnalysisData(ResourceProvider resourceProvider, NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
			DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator) {
		this.resourceProvider = resourceProvider;
		this.nodeCharacteristicsCalculator = nodeCharacteristicsCalculator;
		this.variableCharacteristicsCalculator = variableCharacteristicsCalculator;
	}
	
	public ResourceProvider getResourceProvider() {
		return resourceProvider;
	}
	
	public NodeCharacteristicsCalculator getNodeCharacteristicsCalculator() {
		return nodeCharacteristicsCalculator;
	}
	
	public DataCharacteristicsCalculatorFactory getVariableCharacteristicsCalculator() {
		return variableCharacteristicsCalculator;
	}
}
