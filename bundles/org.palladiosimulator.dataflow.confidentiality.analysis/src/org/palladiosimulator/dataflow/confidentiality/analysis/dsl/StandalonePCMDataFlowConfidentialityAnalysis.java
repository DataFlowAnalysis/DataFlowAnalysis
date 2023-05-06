package org.palladiosimulator.dataflow.confidentiality.analysis.dsl;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm.PCMAnalysisBuilderData;

public class StandalonePCMDataFlowConfidentialityAnalysis extends AbstractStandalonePCMDataFlowConfidentialityAnalysis {

	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and required model paths
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Class of the project plugin
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @param relativeNodeCharacteristicsPath Relative path to the node characteristics model
	 */
	public StandalonePCMDataFlowConfidentialityAnalysis(PCMAnalysisBuilderData builderData, AnalysisData analysisData) {
		super(analysisData,
				Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class),
				builderData.getModelProjectName(), builderData.getPluginActivator());   
    }

	/**
	 * Empty as no additional setup is needed
	 */
	@Override
	public boolean setupAnalysis() {
		return true;
	}

}
