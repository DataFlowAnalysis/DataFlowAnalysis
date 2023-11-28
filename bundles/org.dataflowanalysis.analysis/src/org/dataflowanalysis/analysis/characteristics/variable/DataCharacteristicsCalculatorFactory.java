package org.dataflowanalysis.analysis.characteristics.variable;

import java.util.List;

import org.dataflowanalysis.analysis.characteristics.CharacteristicValue;
import org.dataflowanalysis.analysis.characteristics.DataFlowVariable;

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
