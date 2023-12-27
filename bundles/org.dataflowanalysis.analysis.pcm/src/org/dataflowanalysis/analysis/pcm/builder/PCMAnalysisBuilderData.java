package org.dataflowanalysis.analysis.pcm.builder;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.pcm.core.PCMDataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.pcm.core.PCMNodeCharacteristicsCalculator;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.pcm.resource.PCMURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;

public class PCMAnalysisBuilderData extends AnalysisBuilderData {
	private final Logger logger = Logger.getLogger(PCMAnalysisBuilderData.class);
	
	private String relativeUsageModelPath;
	private String relativeAllocationModelPath;
	private String relativeNodeCharacteristicsPath;
	private Optional<PCMResourceProvider> customResourceProvider = Optional.empty();
	
	/**
	 * Validates the saved data
	 * @throws IllegalStateException Saved data is invalid
	 */
	public void validateData() {
		if (this.getRelativeUsageModelPath().isEmpty() && this.customResourceProvider.isEmpty()) {
			throw new IllegalStateException("A path to a usage model is required");
		}
		if (this.getRelativeAllocationModelPath().isEmpty() && this.customResourceProvider.isEmpty()) {
			throw new IllegalStateException("A path to an allocation model is required");
		}
		if (this.customResourceProvider.isPresent() && !this.customResourceProvider.get().sufficientResourcesLoaded()) {
			throw new IllegalStateException("Custom resource provider did not load all required resources");
		}
		if (this.getRelativeNodeCharacteristicsPath() == null || this.getRelativeNodeCharacteristicsPath().isEmpty()) {
			logger.warn("Using node characteristic model without specifying path to the assignment model. No node characteristics will be applied!");
		}
	}
	

	/**
	 * Creates a new analysis data object from the configured data. It does not check, whether all parameters are set correctly. Use {@code DataFlowConfidentialityAnalysisBuilder} instead
	 * @return Returns a new data object for the analysis
	 */
	public AnalysisData createAnalysisData() {
		PCMResourceProvider resourceProvider = this.getEffectiveResourceProvider();
		return new AnalysisData(resourceProvider, new PCMNodeCharacteristicsCalculator(resourceProvider), new PCMDataCharacteristicsCalculatorFactory(resourceProvider));
	}
	
	/**
	 * Determines the effective resource provider for the analysis.
	 * If a custom resource provider was provided, it will always be used
	 * @return Returns the effective resource provider for the analysis
	 */
	private PCMResourceProvider getEffectiveResourceProvider() {
		return this.customResourceProvider.orElseGet(this::getURIResourceProvider);
	}
	
	/**
	 * Creates a new URI resource loader with the given (optional) node characteristic URI
	 * @param nodeCharacteristicsURI Optional URI to the node characteristics model
	 * @return New instance of an URI resource loader with the internally saved values
	 */
	private PCMResourceProvider getURIResourceProvider() {
		return new PCMURIResourceProvider(ResourceUtils.createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
				ResourceUtils.createRelativePluginURI(relativeAllocationModelPath, modelProjectName), 
				ResourceUtils.createRelativePluginURI(relativeNodeCharacteristicsPath, modelProjectName));
	}
	
	/**
	 * Sets the relative path to the usage model used in the analysis
	 * @param relativeUsageModelPath Relative path to the usage model
	 */
	public void setRelativeUsageModelPath(String relativeUsageModelPath) {
		this.relativeUsageModelPath = relativeUsageModelPath;
	}
	
	/**
	 * Returns the configured relative path to the usage model of the analysis
	 * @return Relative path to the usage model
	 */
	public String getRelativeUsageModelPath() {
		return relativeUsageModelPath;
	}
	
	/**
	 * Sets the relative path to the allocation model used in the analysis
	 * @param relativeAllocationModelPath Relative path to the allocation model
	 */
	public void setRelativeAllocationModelPath(String relativeAllocationModelPath) {
		this.relativeAllocationModelPath = relativeAllocationModelPath;
	}
	
	/**
	 * Returns the configured relative path to the allocation model
	 * @return Relative path to the allocation model
	 */
	public String getRelativeAllocationModelPath() {
		return relativeAllocationModelPath;
	}
	
	/**
	 * Sets the relative path to the node characteristics model that is used in the analysis
	 * @param relativeNodeCharacteristicsPath Relative path to the node characteristics model
	 */
	public void setRelativeNodeCharacteristicsPath(String relativeNodeCharacteristicsPath) {
		this.relativeNodeCharacteristicsPath = relativeNodeCharacteristicsPath;
	}
	
	/**
	 * Returns the relative path to the node characteristics model the analysis is configured to use
	 * @return Relative path to the node characteristcs model
	 */
	public String getRelativeNodeCharacteristicsPath() {
		return relativeNodeCharacteristicsPath;
	}
	
	/**
	 * Registers a custom resource provider for the analysis
	 * @param resourceProvider Custom resource provider of the analysis
	 */
	public void setCustomResourceProvider(PCMResourceProvider resourceProvider) {
		this.customResourceProvider = Optional.of(resourceProvider);
	}
	
	/**
	 * Returns the saved custom resource provider, if it exists
	 * @return Returns an Optional containing the resource provider, if one was specified
	 */
	public Optional<PCMResourceProvider> getCustomResourceProvider() {
		return this.customResourceProvider;
	}
}
