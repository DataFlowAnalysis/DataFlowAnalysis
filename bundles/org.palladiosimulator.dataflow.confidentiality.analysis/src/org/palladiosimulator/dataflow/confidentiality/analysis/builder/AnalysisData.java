package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.DataCharacteristicsCalculatorFactory;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceProvider;

public record AnalysisData(ResourceProvider resourceLoader, 
		NodeCharacteristicsCalculator nodeCharacteristicsCalculator,
		DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator) {
	
	public AnalysisData(ResourceProvider resourceLoader, NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
			DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator) {
		this.resourceLoader = resourceLoader;
		this.nodeCharacteristicsCalculator = nodeCharacteristicsCalculator;
		this.variableCharacteristicsCalculator = variableCharacteristicsCalculator;
	}
	
	public ResourceProvider getResourceLoader() {
		return resourceLoader;
	}
	
	public NodeCharacteristicsCalculator getNodeCharacteristicsCalculator() {
		return nodeCharacteristicsCalculator;
	}
	
	public DataCharacteristicsCalculatorFactory getVariableCharacteristicsCalculator() {
		return variableCharacteristicsCalculator;
	}
}
