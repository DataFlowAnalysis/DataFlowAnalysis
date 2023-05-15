	package org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm;

import java.util.List;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AbstractDataFlowAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisBuilderData;
import org.palladiosimulator.dataflow.confidentiality.analysis.core.AbstractStandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.core.LegacyStandalonePCMDataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.core.StandalonePCMDataFlowConfidentialityAnalysis;

public class PCMDataFlowConfidentialityAnalysisBuilder 
extends AbstractDataFlowAnalysisBuilder<AbstractStandalonePCMDataFlowConfidentialityAnalysis, PCMAnalysisBuilderData, AnalysisBuilderData> {

	public PCMDataFlowConfidentialityAnalysisBuilder() {
		super(new PCMAnalysisBuilderData());
	}
	
	/**
	 * Set the legacy mode of the analysis
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder legacy() {
		this.builderData.setLegacy(true);
		return this;
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
	 * Uses the given resource to load the model contents
	 * @param resource Resource containing model contents
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useResource(Resource resource) {
		this.builderData.addResource(resource);
		return this;
	}
	
	/**
	 * Uses the given resources to load the model contents
	 * @param resources Resources containing model contents
	 * @return Returns builder object of the analysis
	 */
	public PCMDataFlowConfidentialityAnalysisBuilder useResources(List<Resource> resources) {
		resources.forEach(it ->	this.builderData.addResource(it));
		return this;
	}
	
	@Override
	public void copyBuilderData(AnalysisBuilderData builderData) {
		super.builderData.setModelProjectName(builderData.getModelProjectName());
		super.builderData.setStandalone(builderData.isStandalone());
	}
	
	@Override
	public void validateBuilderData() {
		this.builder.forEach(it -> it.validateBuilderData());
		this.builderData.validateData();
	}

	@Override
	public AbstractStandalonePCMDataFlowConfidentialityAnalysis build() {
		this.validateBuilderData();
		if (this.builderData.isLegacy()) {
			return new LegacyStandalonePCMDataFlowConfidentialityAnalysis(builderData.getModelProjectName(),
					builderData.getPluginActivator(), builderData.createAnalysisData());
		} else {
			return new StandalonePCMDataFlowConfidentialityAnalysis(builderData.getModelProjectName(),
					builderData.getPluginActivator(), builderData.createAnalysisData());
		}
	}
}
