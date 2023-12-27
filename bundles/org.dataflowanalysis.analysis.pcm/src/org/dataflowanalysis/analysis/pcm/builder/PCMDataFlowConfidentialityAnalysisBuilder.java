package org.dataflowanalysis.analysis.pcm.builder;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.validation.ValidationError;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.eclipse.core.runtime.Plugin;

public class PCMDataFlowConfidentialityAnalysisBuilder 
extends DataFlowAnalysisBuilder {
	private final Logger logger = Logger.getLogger(PCMDataFlowConfidentialityAnalysisBuilder.class);
	
	private PCMAnalysisBuilderData builderData;

	public PCMDataFlowConfidentialityAnalysisBuilder() {
		this.builderData = new PCMAnalysisBuilderData();
	}
	
	/**
	 * Uses a plugin activator class for the given project
	 * @param pluginActivator Plugin activator class of the modeling project
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
		this.builderData.setPluginActivator(Optional.of(pluginActivator));
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
	public PCMDataFlowConfidentialityAnalysisBuilder useCustomResourceProvider(PCMResourceProvider resourceProvider) {
		this.builderData.setCustomResourceProvider(resourceProvider);
		return this;
	}
	
	/**
	 * Sets standalone mode of the analysis
	 * @return Builder of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	/**
	 * Sets the modeling project name of the analysis
	 * @return Builder of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder modelProjectName(String modelProjectName) {
		this.builderData.setModelProjectName(modelProjectName);
		return this;
	}

	@Override
	public PCMDataFlowConfidentialityAnalysis build() {
		List<ValidationError> validationErrors = this.builderData.validate();
		validationErrors.forEach(it -> it.log(logger));
		if (validationErrors.stream().anyMatch(ValidationError::isFatal)) {
			logger.fatal("Could not create analysis due to fatal validation errors");
			throw new IllegalStateException();
		}
		return new PCMDataFlowConfidentialityAnalysis(builderData.createAnalysisData(), builderData.getModelProjectName(),
				builderData.getPluginActivator());
	}
}
