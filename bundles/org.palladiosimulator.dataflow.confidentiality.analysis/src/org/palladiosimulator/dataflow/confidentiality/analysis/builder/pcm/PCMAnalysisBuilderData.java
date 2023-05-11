package org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisBuilderData;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.LegacyPCMNodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.node.PCMNodeCharacteristicsCalculatorImpl;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.variable.PCMDataCharacteristicsCalculatorFactory;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMURIResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.ResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.PCMResourceUtils;

public class PCMAnalysisBuilderData extends AnalysisBuilderData {
	private final Logger logger = Logger.getLogger(PCMAnalysisBuilderData.class);
	
	private Class<? extends Plugin> pluginActivator;
	private String relativeUsageModelPath;
	private String relativeAllocationModelPath;
	private String relativeNodeCharacteristicsPath;
	private boolean legacy;
	
	/**
	 * Validates the saved data
	 * @throws IllegalStateException Saved data is invalid
	 */
	public void validateData() {
		if (this.getPluginActivator() == null) {
			throw new IllegalStateException("A plugin activator is required");
		}
		if (this.getRelativeUsageModelPath().isEmpty()) {
			throw new IllegalStateException("A path to a usage model is required");
		}
		if (this.getRelativeAllocationModelPath().isEmpty()) {
			throw new IllegalStateException("A path to an allocation model is required");
		}
		if (this.isLegacy()) {
			logger.info("Using legacy EMF Profiles for Node Characteristic application");
		}
		if (!this.isLegacy() && this.getRelativeNodeCharacteristicsPath().isEmpty()) {
			logger.warn("Using new node characteristic model without specifying path to the assignment model. No node characteristics will be applied!");
		}
	}
	

	/**
	 * Creates a new analysis data object from the configured data. It does not check, whether all parameters are set correctly. Use {@code DataFlowConfidentialityAnalysisBuilder} instead
	 * @return Returns a new data object for the analysis
	 */
	public AnalysisData createAnalysisData() {
		if (this.isLegacy()) {
			ResourceLoader resourceLoader = new PCMURIResourceLoader(PCMResourceUtils.createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
					PCMResourceUtils.createRelativePluginURI(relativeAllocationModelPath, modelProjectName), Optional.empty());
			return new AnalysisData(resourceLoader, 
					new LegacyPCMNodeCharacteristicsCalculator(resourceLoader), new PCMDataCharacteristicsCalculatorFactory(resourceLoader));
		} else {
			ResourceLoader resourceLoader = new PCMURIResourceLoader(PCMResourceUtils.createRelativePluginURI(relativeUsageModelPath, modelProjectName), 
					PCMResourceUtils.createRelativePluginURI(relativeAllocationModelPath, modelProjectName), 
					Optional.of(PCMResourceUtils.createRelativePluginURI(relativeNodeCharacteristicsPath, modelProjectName)));
			return new AnalysisData(resourceLoader, 
					new PCMNodeCharacteristicsCalculatorImpl(resourceLoader), new PCMDataCharacteristicsCalculatorFactory(resourceLoader));
		}
	}
	
	/**
	 * Sets the plugin activator of the project
	 * @param pluginActivator Eclipse plugin activator class
	 */
	public void setPluginActivator(Class<? extends Plugin> pluginActivator) {
		this.pluginActivator = pluginActivator;
	}
	
	/**
	 * Returns the plugin activator of the project
	 * @return Eclipse plugin activator class of the project
	 */
	public Class<? extends Plugin> getPluginActivator() {
		return pluginActivator;
	}
	
	/**
	 * Sets the legacy mode of the analysis to allow the loading of EMF Profiles
	 * @param legacy New value of the legacy mode
	 */
	public void setLegacy(boolean legacy) {
		this.legacy = legacy;
	}
	
	/**
	 * Returns, whether or not the analysis is in legacy mode
	 * @return Returns true, if the analysis is in legacy mode and EMF Profiles are loaded. Otherwise, the method returns false
	 */
	public boolean isLegacy() {
		return legacy;
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
}
