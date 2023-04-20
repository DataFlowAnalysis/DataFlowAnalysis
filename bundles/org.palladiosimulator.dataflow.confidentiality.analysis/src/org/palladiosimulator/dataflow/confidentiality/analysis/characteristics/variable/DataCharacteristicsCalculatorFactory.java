package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.DataFlowVariable;

public interface DataCharacteristicsCalculatorFactory {
	/**
     * Initialize Characteristic Calculator with initial variables.
     * 
     * @param initialVariables DataFlowVariables of the previous ActionSequence Element
     * @param nodeCharacteristics Node characteristics applied to the node
     */
	public DataCharacteristicsCalculator createNodeCalculator(List<DataFlowVariable> initialVariables, 
    		List<CharacteristicValue> nodeCharacteristics);
}
