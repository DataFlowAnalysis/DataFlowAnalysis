package org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm;

import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AbsrtractDataFlowConfidentialityAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.DataFlowConfidentialityAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.LegacyStandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.StandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.StandalonePCMDataFlowConfidentialityAnalysisImpl;

public class PCMDataFlowConfidentialityAnalysisBuilder extends AbsrtractDataFlowConfidentialityAnalysisBuilder<StandalonePCMDataFlowConfidentialityAnalysis, PCMAnalysisBuilderData> {
	private AbsrtractDataFlowConfidentialityAnalysisBuilder<?,?> previousBuilder;

	public PCMDataFlowConfidentialityAnalysisBuilder(DataFlowConfidentialityAnalysisBuilder builder) {
		super(new PCMAnalysisBuilderData(builder.getBuilderData()));
		this.previousBuilder = builder;
	}
	
	/**
	 * Set the legacy mode of the analysis
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder legacy() {
		this.builderData.setLegacy(true);
		return this;
	}
	
	public PCMDataFlowConfidentialityAnalysisBuilder registerPluginActivator(Class<? extends Plugin> pluginActivator) {
		this.builderData.setPluginActivator(pluginActivator);
		return this;
	}
	
	/**
	 * Register a new path for a usage model
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder registerUsageModel(String relativeUsageModelPath) {
		this.builderData.setRelativeUsageModelPath(relativeUsageModelPath);
		return this;
	}
	
	/**
	 * Register a new path for an allocation model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder registerAllocationModel(String relativeAllocationModelPath) {
		this.builderData.setRelativeAllocationModelPath(relativeAllocationModelPath);
		return this;
	}
	
	/**
	 * Register a new path for node characteristics
	 * @param relativeNodeCharacteristicsModelPath Relative path to the node characteristics model
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder registerNodeCharacteristicsModel(String relativeNodeCharacteristicsModelPath) {
		this.builderData.setRelativeNodeCharacteristicsPath(relativeNodeCharacteristicsModelPath);
		return this;
	}
	

	@Override
	public void checkBuilderData() {
		previousBuilder.checkBuilderData();
		if (this.builderData.getPluginActivator() == null) {
			throw new IllegalStateException("A plugin activator is required");
		}
		if (this.builderData.getRelativeUsageModelPath().isEmpty()) {
			throw new IllegalStateException("A path to a usage model is required");
		}
		if (this.builderData.getRelativeAllocationModelPath().isEmpty()) {
			throw new IllegalStateException("A path to an allocation model is required");
		}
		if (this.builderData.isLegacy()) {
			logger.warn("Using legacy EMF Profiles for Node Characteristic application");
		}
		if (!this.builderData.isLegacy() && this.builderData.getRelativeNodeCharacteristicsPath().isEmpty()) {
			logger.warn("Using new node characteristic model without specifying path to the assignment model. No node characteristics will be applied!");
		}
	}

	@Override
	public StandalonePCMDataFlowConfidentialityAnalysis build() {
		this.checkBuilderData();
		if (this.builderData.isLegacy()) {
			return new LegacyStandalonePCMDataFlowConfidentialityAnalysis(builderData, builderData.createAnalysisData());
		} else {
			return new StandalonePCMDataFlowConfidentialityAnalysisImpl(builderData, builderData.createAnalysisData());
		}
	}

}
