package org.palladiosimulator.dataflow.confidentiality.analysis.core;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceProvider;

public class StandalonePCMDataFlowConfidentialityAnalysis extends AbstractStandalonePCMDataFlowConfidentialityAnalysis {

	/**
	 * Creates a new DataFlowConfidentialityAnlysis with the given modelling project name, modelling project plugin instance and global analysis data
	 * @param modelProjectName Name of the modelling project
	 * @param pluginActivator Plugin activator class of the modelling project
	 * @param analysisData Global analysis data that should be used
	 */
	public StandalonePCMDataFlowConfidentialityAnalysis(String modelProjectName, Class<? extends Plugin> pluginActivator, AnalysisData analysisData) {
		super(analysisData,
				Logger.getLogger(LegacyStandalonePCMDataFlowConfidentialityAnalysis.class),
				modelProjectName, pluginActivator);   
    }

	/**
	 * Empty as no additional setup is needed
	 */
	@Override
	public boolean setupAnalysis() {
		return true;
	}
}
