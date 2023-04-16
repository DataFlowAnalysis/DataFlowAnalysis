package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.VariableCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;

public class AnalysisData {
	private final ResourceLoader resourceLoader;
	private final NodeCharacteristicsCalculator nodeCharacteristicsCalculator;
	private final VariableCharacteristicsCalculator variableCharacteristicsCalculator;
	
	public AnalysisData(ResourceLoader resourceLoader, NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
			VariableCharacteristicsCalculator variableCharacteristicsCalculator) {
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
	
	public VariableCharacteristicsCalculator getVariableCharacteristicsCalculator() {
		return variableCharacteristicsCalculator;
	}
}
