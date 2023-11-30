package org.dataflowanalysis.analysis.builder.pcm;

import org.dataflowanalysis.analysis.builder.AbstractDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.core.StandalonePCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.eclipse.core.runtime.Plugin;

public class PCMDataFlowConfidentialityAnalysisBuilder 
extends AbstractDataFlowAnalysisBuilder<StandalonePCMDataFlowConfidentialityAnalysis, PCMAnalysisBuilderData, AnalysisBuilderData> {

	public PCMDataFlowConfidentialityAnalysisBuilder() {
		super(new PCMAnalysisBuilderData());
	}
	
	/**
	 * Uses a plugin activator class for the given project
	 * @param pluginActivator Plugin activator class of the modeling project
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
		this.builderData.setPluginActivator(pluginActivator);
		return this;
	}
	
	/**
	 * Uses a new path for a usage model
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useUsageModel(String relativeUsageModelPath) {
		this.builderData.setRelativeUsageModelPath(relativeUsageModelPath);
		return this;
	}
	
	/**
	 * Uses a new path for an allocation model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useAllocationModel(String relativeAllocationModelPath) {
		this.builderData.setRelativeAllocationModelPath(relativeAllocationModelPath);
		return this;
	}
	
	/**
	 * Uses a new path for node characteristics
	 * @param relativeNodeCharacteristicsModelPath Relative path to the node characteristics model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useNodeCharacteristicsModel(String relativeNodeCharacteristicsModelPath) {
		this.builderData.setRelativeNodeCharacteristicsPath(relativeNodeCharacteristicsModelPath);
		return this;
	}
	
	/**
	 * Uses a new path for node characteristics
	 * @param relativeNodeCharacteristicsModelPath Relative path to the node characteristics model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useCustomResourceProvider(ResourceProvider resourceProvider) {
		this.builderData.setCustomResourceProvider(resourceProvider);
		return this;
	}
	
	@Override
	public void copyBuilderData(AnalysisBuilderData builderData) {
		super.builderData.setModelProjectName(builderData.getModelProjectName());
		super.builderData.setStandalone(builderData.isStandalone());
	}
	
	@Override
	public void validateBuilderData() {
		this.builder.forEach(AbstractDataFlowAnalysisBuilder::validateBuilderData);
		this.builderData.validateData();
	}

	@Override
	public StandalonePCMDataFlowConfidentialityAnalysis build() {
		this.validateBuilderData();
		return new StandalonePCMDataFlowConfidentialityAnalysis(builderData.createAnalysisData(), builderData.getModelProjectName(),
				builderData.getPluginActivator());
	}
}
