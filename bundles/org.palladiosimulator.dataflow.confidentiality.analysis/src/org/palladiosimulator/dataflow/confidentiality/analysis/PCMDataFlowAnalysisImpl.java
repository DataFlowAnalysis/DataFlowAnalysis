package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.nio.file.Paths;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMURIResourceLoader;

public class PCMDataFlowAnalysisImpl extends PCMDataFlowAnalysis {

	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and required model paths
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Class of the project plugin
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @param relativeNodeCharacteristicsPath Relative path to the node characteristics model
	 */
	public PCMDataFlowAnalysisImpl(String modelProjectName,
            Class<? extends Plugin> modelProjectActivator, String relativeUsageModelPath,
            String relativeAllocationModelPath, String relativeNodeCharacteristicsPath) {
		super(createResourceLoader(modelProjectName, relativeUsageModelPath, relativeAllocationModelPath, relativeNodeCharacteristicsPath),
				Logger.getLogger(LegacyPCMDataFlowConfidentialityAnalysis.class),
				modelProjectName, modelProjectActivator);   
    }
	

	/**
	 * Creates a resource loader with the given model project name and the paths to the usage and allocation model
	 * @param modelProjectName Name of the modelling project
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @param relativeNodeCharacteristicsPath Relative path to the node characteristics model
	 * @return Returns new instance of an PCMRUIResourceLoader with the given paths, allowing the analysis to load the required resources
	 */
	private static PCMResourceLoader createResourceLoader(String modelProjectName, 
			String relativeUsageModelPath, String relativeAllocationModelPath, String relativeNodeCharacteristicsPath) {
		return new PCMURIResourceLoader(createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
        		createRelativePluginURI(relativeAllocationModelPath, modelProjectName), Optional.of(createRelativePluginURI(relativeNodeCharacteristicsPath, modelProjectName)));
	}
    
    /**
     * Creates a relative plugin uri from the given relative path
     * @param relativePath Given relative path
     * @return Returns plugin path with the given project name and provided relative path
     */
    private static URI createRelativePluginURI(String relativePath, String modelProjectName) {
        String path = Paths.get(modelProjectName, relativePath)
            .toString();
        return URI.createPlatformPluginURI(path, false);
    }

	/**
	 * Empty as no additional setup is needed
	 */
	@Override
	public boolean setupAnalysis() {
		return true;
	}

}
