package org.palladiosimulator.dataflow.confidentiality.analysis.dsl;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisBuilderData;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm.PCMAnalysisBuilderData;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.AnalysisConstants;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;

public class LegacyStandalonePCMDataFlowConfidentialityAnalysis extends AbstractStandalonePCMDataFlowConfidentialityAnalysis {
	private final Logger logger = Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class);
	
	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and required model paths
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Class of the project plugin
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 */
	public LegacyStandalonePCMDataFlowConfidentialityAnalysis(PCMAnalysisBuilderData builderData, AnalysisData analysisData) {
		super(analysisData,
				Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class),
				builderData.getModelProjectName(), builderData.getPluginActivator());   
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
