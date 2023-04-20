package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.DataCharacteristicsCalculatorFactory;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;

public class AnalysisData {
	private final ResourceLoader resourceLoader;
	private final NodeCharacteristicsCalculator nodeCharacteristicsCalculator;
	private final DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator;
	
	public AnalysisData(ResourceLoader resourceLoader, NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
			DataCharacteristicsCalculatorFactory variableCharacteristicsCalculator) {
		this.resourceLoader = resourceLoader;
		this.nodeCharacteristicsCalculator = nodeCharacteristicsCalculator;
		this.variableCharacteristicsCalculator = variableCharacteristicsCalculator;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}
	
	public NodeCharacteristicsCalculator getNodeCharacteristicsCalculator() {
		return nodeCharacteristicsCalculator;
	}
	
	public DataCharacteristicsCalculatorFactory getVariableCharacteristicsCalculator() {
		return variableCharacteristicsCalculator;
	}
}
