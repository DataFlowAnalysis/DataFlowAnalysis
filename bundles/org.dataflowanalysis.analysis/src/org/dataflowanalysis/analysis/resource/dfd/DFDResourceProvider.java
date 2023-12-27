package org.dataflowanalysis.analysis.resource.dfd;

import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

public interface DFDResourceProvider extends ResourceProvider {
	/**
	 * Returns the data flow diagram model that the resource loader has loaded
	 * @return Data flow diagram model saved in the resources
	 */
	public DataFlowDiagram getDataFlowDiagram();
	
	/**
	 * Returns the data dictionary model that the resource loader has loaded
	 * @return Data dictionary model saved in the resources
	 */
	public DataDictionary getDataDictionary();
	
	/**
	 * Determines, whether the resource loader has sufficient resources to run the analysis
	 * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method returns false
	 */
	public default boolean sufficientResourcesLoaded() {
		if (this.getDataFlowDiagram() == null || this.getDataDictionary() == null) {
			return false;
		}
		return true;
	}
}
