package org.palladiosimulator.dataflow.confidentiality.analysis.core;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;

public class LegacyStandalonePCMDataFlowConfidentialityAnalysis extends AbstractStandalonePCMDataFlowConfidentialityAnalysis {
	private static final String EMF_PROFILE_NAME = "profile.emfprofile_diagram";
	private static final String EMF_PROFILE_PLUGIN = "org.palladiosimulator.dataflow.confidentiality.pcm.model.profile";
	
	private final Logger logger = Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class);
	
	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and global analysis data
	 * @param modelProjectName Name of the modelling project
	 * @param pluginActivator Plugin activator class of the modelling project
	 * @param analysisData Global analysis data that should be used
	 */
	public LegacyStandalonePCMDataFlowConfidentialityAnalysis(String modelProjectName, Class<? extends Plugin> pluginActivator, AnalysisData analysisData) {
		super(analysisData,
				Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class),
				modelProjectName, pluginActivator);   
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
            new EMFProfileInitializationTask(LegacyStandalonePCMDataFlowConfidentialityAnalysis.EMF_PROFILE_PLUGIN, 
            		LegacyStandalonePCMDataFlowConfidentialityAnalysis.EMF_PROFILE_NAME)
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