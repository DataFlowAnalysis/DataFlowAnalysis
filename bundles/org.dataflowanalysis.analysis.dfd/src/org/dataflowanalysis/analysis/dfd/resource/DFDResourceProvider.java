package org.dataflowanalysis.analysis.dfd.resource;

import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

/**
 * This abstract class represents the required model data that is required to run a dfd analysis
 */
public abstract class DFDResourceProvider extends ResourceProvider {
    /**
     * Returns the data flow diagram model that the resource loader has loaded
     * @return Data flow diagram model saved in the resources
     */
    public abstract DataFlowDiagram getDataFlowDiagram();

    /**
     * Returns the data dictionary model that the resource loader has loaded
     * @return Data dictionary model saved in the resources
     */
    public abstract DataDictionary getDataDictionary();

    /**
     * Determines, whether the resource loader has sufficient resources to run the analysis
     * @return This method returns true, if the analysis can be executed with the resource loader. Otherwise, the method
     * returns false
     */
    @Override
    public boolean sufficientResourcesLoaded() {
        return this.getDataFlowDiagram() != null && this.getDataDictionary() != null;
    }
}
