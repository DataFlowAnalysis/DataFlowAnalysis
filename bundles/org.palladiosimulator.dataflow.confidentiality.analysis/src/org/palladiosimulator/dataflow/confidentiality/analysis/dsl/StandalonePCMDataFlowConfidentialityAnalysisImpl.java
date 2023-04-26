package org.palladiosimulator.dataflow.confidentiality.analysis.dsl;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisBuilderData;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;

public class StandalonePCMDataFlowConfidentialityAnalysisImpl extends StandalonePCMDataFlowConfidentialityAnalysis {

	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and required model paths
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Class of the project plugin
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @param relativeNodeCharacteristicsPath Relative path to the node characteristics model
	 */
	public StandalonePCMDataFlowConfidentialityAnalysisImpl(AnalysisBuilderData builderData, AnalysisData analysisData) {
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
