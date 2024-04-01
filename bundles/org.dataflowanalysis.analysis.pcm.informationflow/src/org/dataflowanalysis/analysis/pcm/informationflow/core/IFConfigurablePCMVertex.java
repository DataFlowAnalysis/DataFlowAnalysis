package org.dataflowanalysis.analysis.pcm.informationflow.core;

import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;

/**
 * Defines configuration options for the extraction of label propagation
 * functions of a vertex from the PCM.
 *
 */
public interface IFConfigurablePCMVertex {

	/**
	 * Specifies whether the vertex should consider implicit flows.
	 * 
	 * @param consider whether the vertex should consider implicit flows
	 */
	public void setConsiderImplicitFlow(boolean consider);

	/**
	 * Specifies the extraction strategy for label propagation functions from
	 * VariableCharacterisations.
	 * 
	 * @param extractionStrategy the strategy
	 */
	public void setExtractionStrategy(IFPCMExtractionStrategy extractionStrategy);

	/**
	 * Returns whether the vertex considers implicit flow.
	 * 
	 * @return whether the vertex considers implicit flow
	 */
	public boolean isConsideringImplicitFlow();

	/**
	 * Returns the used extraction strategy for label propagation functions from
	 * VariableCharacterisations.
	 * 
	 * @return the used extraction strategy
	 */
	public IFPCMExtractionStrategy getExtractionStrategy();

}
