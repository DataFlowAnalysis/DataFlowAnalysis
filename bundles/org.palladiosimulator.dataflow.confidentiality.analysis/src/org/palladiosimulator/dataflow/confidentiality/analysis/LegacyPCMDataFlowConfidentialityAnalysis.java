package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.nio.file.Paths;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMURIResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.AnalysisConstants;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;

public class LegacyPCMDataFlowConfidentialityAnalysis extends PCMDataFlowAnalysis {
	private final Logger logger = Logger.getLogger(LegacyPCMDataFlowConfidentialityAnalysis.class);
	
	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and required model paths
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Class of the project plugin
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 */
	public LegacyPCMDataFlowConfidentialityAnalysis(String modelProjectName,
            Class<? extends Plugin> modelProjectActivator, String relativeUsageModelPath,
            String relativeAllocationModelPath) {
		super(createResourceLoader(modelProjectName, relativeUsageModelPath, relativeAllocationModelPath),
				Logger.getLogger(LegacyPCMDataFlowConfidentialityAnalysis.class),
				modelProjectName, modelProjectActivator);   
    }
	

	/**
	 * Creates a resource loader with the given model project name and the paths to the usage and allocation model
	 * @param modelProjectName Name of the modelling project
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @return Returns new instance of an PCMRUIResourceLoader with the given paths, allowing the analysis to load the required resources
	 */
	private static PCMResourceLoader createResourceLoader(String modelProjectName, String relativeUsageModelPath, String relativeAllocationModelPath) {
		return new PCMURIResourceLoader(createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
        		createRelativePluginURI(relativeAllocationModelPath, modelProjectName), Optional.empty());
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


	@Override
	public boolean setupAnalysis() {
		return this.initEMFProfiles();
	}
	
	/**
     * Initializes the EMF Profiles support of the analysis
     * @return
     */
    private boolean initEMFProfiles() {
        try {
            new EMFProfileInitializationTask(AnalysisConstants.EMF_PROFILE_PLUGIN, AnalysisConstants.EMF_PROFILE_NAME)
                .initilizationWithoutPlatform();
            logger.info("Successfully initialized standalone EMF Profiles for the data flow analysis.");
            return true;
        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone EMF Profile for the data flow analysis.");
            e.printStackTrace();
            return false;
        }
    }
}
