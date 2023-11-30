package org.dataflowanalysis.analysis.builder.pcm;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.characteristics.node.PCMNodeCharacteristicsCalculator;
import org.dataflowanalysis.analysis.characteristics.variable.PCMDataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.resource.PCMURIResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.analysis.utils.pcm.PCMResourceUtils;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;

public class PCMAnalysisBuilderData extends AnalysisBuilderData {
	private final Logger logger = Logger.getLogger(PCMAnalysisBuilderData.class);
	
	private Optional<Class<? extends Plugin>> pluginActivator;
	private String relativeUsageModelPath;
	private String relativeAllocationModelPath;
	private String relativeNodeCharacteristicsPath;
	private Optional<ResourceProvider> customResourceProvider = Optional.empty();
	
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
		ResourceProvider resourceProvider = this.getEffectiveResourceProvider();
		return new AnalysisData(resourceProvider, new PCMNodeCharacteristicsCalculator(resourceProvider), new PCMDataCharacteristicsCalculatorFactory(resourceProvider));
	}
	
	/**
	 * Determines the effective resource provider for the analysis.
	 * If a custom resource provider was provided, it will always be used
	 * @return Returns the effective resource provider for the analysis
	 */
	private ResourceProvider getEffectiveResourceProvider() {
		return this.customResourceProvider.orElseGet(this::getURIResourceProvider);
	}
	
	/**
	 * Creates a new URI resource loader with the given (optional) node characteristic URI
	 * @param nodeCharacteristicsURI Optional URI to the node characteristics model
	 * @return New instance of an URI resource loader with the internally saved values
	 */
	private ResourceProvider getURIResourceProvider() {
		return new PCMURIResourceProvider(PCMResourceUtils.createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
				PCMResourceUtils.createRelativePluginURI(relativeAllocationModelPath, modelProjectName), 
				PCMResourceUtils.createRelativePluginURI(relativeNodeCharacteristicsPath, modelProjectName));
	}
	
	/**
	 * Sets the plugin activator of the project
	 * @param pluginActivator Eclipse plugin activator class
	 */
	public void setPluginActivator(Optional<Class<? extends Plugin>> pluginActivator) {
		this.pluginActivator = pluginActivator;
	}
	
	/**
	 * Returns the plugin activator of the project
	 * @return Eclipse plugin activator class of the project
	 */
	public Optional<Class<? extends Plugin>> getPluginActivator() {
		return pluginActivator;
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
	public void setCustomResourceProvider(ResourceProvider resourceProvider) {
		this.customResourceProvider = Optional.of(resourceProvider);
	}
	
	/**
	 * Returns the saved custom resource provider, if it exists
	 * @return Returns an Optional containing the resource provider, if one was specified
	 */
	public Optional<ResourceProvider> getCustomResourceProvider() {
		return this.customResourceProvider;
	}
}
