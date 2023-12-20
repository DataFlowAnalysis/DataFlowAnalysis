package org.dataflowanalysis.analysis.core;

import java.util.List;

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
