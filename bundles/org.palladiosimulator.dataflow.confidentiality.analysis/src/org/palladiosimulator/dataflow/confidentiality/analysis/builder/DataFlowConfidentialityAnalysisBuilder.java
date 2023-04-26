package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.LegacyStandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.dsl.StandalonePCMDataFlowConfidentialityAnalysisImpl;

public class DataFlowConfidentialityAnalysisBuilder {
	private final Logger logger = Logger.getLogger(DataFlowConfidentialityAnalysisBuilder.class);
	
	private AnalysisBuilderData builderData;
	
	public DataFlowConfidentialityAnalysisBuilder(String modelProjectName, Class<? extends Plugin> modelProjectActivator) {
		this.builderData = new AnalysisBuilderData();
		this.builderData.setModelProjectName(modelProjectName);
		this.builderData.setPluginActivator(modelProjectActivator);
	}
	
	public DataFlowConfidentialityAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	public DataFlowConfidentialityAnalysisBuilder legacy() {
		this.builderData.setLegacy(true);
		return this;
	}
	
	public DataFlowConfidentialityAnalysisBuilder registerUsageModel(String relativeUsageModelPath) {
		this.builderData.setRelativeUsageModelPath(relativeUsageModelPath);
		return this;
	}
	
	public DataFlowConfidentialityAnalysisBuilder registerAllocationModel(String relativeAllocationModelPath) {
		this.builderData.setRelativeAllocationModelPath(relativeAllocationModelPath);
		return this;
	}
	
	public DataFlowConfidentialityAnalysisBuilder registerNodeCharacteristicsModel(String relativeNodeCharacteristicsModelPath) {
		this.builderData.setRelativeNodeCharacteristicsPath(relativeNodeCharacteristicsModelPath);
		return this;
	}
	
	public DataFlowConfidentialityAnalysis build() {
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
		if (this.builderData.isLegacy()) {
			return new LegacyStandalonePCMDataFlowConfidentialityAnalysis(builderData, builderData.createAnalysisData());
		} else {
			return new StandalonePCMDataFlowConfidentialityAnalysisImpl(builderData, builderData.createAnalysisData());
		}
	}
}
