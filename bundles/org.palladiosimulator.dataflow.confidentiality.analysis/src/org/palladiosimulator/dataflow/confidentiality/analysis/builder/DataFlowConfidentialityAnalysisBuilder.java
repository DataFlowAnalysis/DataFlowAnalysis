package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.LegacyStandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.StandalonePCMDataFlowConfidentialityAnalysisImpl;

public class DataFlowConfidentialityAnalysisBuilder {
	private final Logger logger = Logger.getLogger(DataFlowConfidentialityAnalysisBuilder.class);
	
	private final AnalysisBuilderData builderData;
	
	/**
	 * Creates a new analysis builder with the given project name and project activator
	 * @param modelProjectName Name of the modeling project
	 * @param modelProjectActivator Activator plugin class of the modeling project
	 */
	public DataFlowConfidentialityAnalysisBuilder(String modelProjectName, Class<? extends Plugin> modelProjectActivator) {
		this.builderData = new AnalysisBuilderData();
		this.builderData.setModelProjectName(modelProjectName);
		this.builderData.setPluginActivator(modelProjectActivator);
	}
	
	/**
	 * Sets the analysis mode to standalone
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	/**
	 * Set the legacy mode of the analysis
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder legacy() {
		this.builderData.setLegacy(true);
		return this;
	}
	
	/**
	 * Register a new path for a usage model
	 * @param relativeUsageModelPath Relative path to the usage model
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder registerUsageModel(String relativeUsageModelPath) {
		this.builderData.setRelativeUsageModelPath(relativeUsageModelPath);
		return this;
	}
	
	/**
	 * Register a new path for an allocation model
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder registerAllocationModel(String relativeAllocationModelPath) {
		this.builderData.setRelativeAllocationModelPath(relativeAllocationModelPath);
		return this;
	}
	
	/**
	 * Register a new path for node characteristics
	 * @param relativeNodeCharacteristicsModelPath Relative path to the node characteristics model
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder registerNodeCharacteristicsModel(String relativeNodeCharacteristicsModelPath) {
		this.builderData.setRelativeNodeCharacteristicsPath(relativeNodeCharacteristicsModelPath);
		return this;
	}
	
	/**
	 * Checks the provided data for issues or warnings
	 * @throws IllegalStateException The creation and execution with the current parameters is not permissible
	 */
	private void checkBuilderData() {
		if (!this.builderData.isStandalone()) {
			throw new IllegalStateException("Execution of the analysis is only supported in standalone mode");
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
	
	/**
	 * Create a new {@code DataFlowConfidentialityAnalysis} with the given parameter
	 * @return Returns a new analysis with the given parameter
	 */
	public DataFlowConfidentialityAnalysis build() {
		this.checkBuilderData();
		if (this.builderData.isLegacy()) {
			return new LegacyStandalonePCMDataFlowConfidentialityAnalysis(builderData, builderData.createAnalysisData());
		} else {
			return new StandalonePCMDataFlowConfidentialityAnalysisImpl(builderData, builderData.createAnalysisData());
		}
	}
}
