package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;

public class PCMVariableCharacteristicsCalculator implements VariableCharacteristicsCalculator {
	private ResourceLoader resourceLoader;
	
	/** 
	 * Creates a new instance of the variable characteristics calculator
	 * @param resourceLoader Resource loader the characteristics calculators should use
	 */
	public PCMVariableCharacteristicsCalculator(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	/**
     * Initialize Characteristic Calculator with initial variables.
     * 
     * @param initialVariables DataFlowVariables of the previous ActionSequence Element
     * @param nodeCharacteristics Node characteristics applied to the node
     */
	@Override
	public NodeVariableCharacteristicsCalculator createNodeCalculator(List<DataFlowVariable> initialVariables,
			List<CharacteristicValue> nodeCharacteristics) {
		return new PCMNodeVariableCharacteristicsCalculator(initialVariables, nodeCharacteristics, this.resourceLoader);
	}

}
