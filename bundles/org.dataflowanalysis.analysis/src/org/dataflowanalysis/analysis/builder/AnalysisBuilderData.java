package org.dataflowanalysis.analysis.builder;

import java.util.Optional;

import org.eclipse.core.runtime.Plugin;

public class AnalysisBuilderData {
	protected boolean standalone;
	protected String modelProjectName;
	private Optional<Class<? extends Plugin>> pluginActivator = Optional.empty();
	
	public AnalysisBuilderData() {}
	
	/**
	 * Sets the model project name
	 * @param modelProjectName Project name of the modelling project
	 */
	public void setModelProjectName(String modelProjectName) {
		this.modelProjectName = modelProjectName;
	}
	
	/**
	 * Returns the configured name of the modelling project
	 * @return Saved name of the modelling project
	 */
	public String getModelProjectName() {
		return modelProjectName;
	}
	
	/**
	 * Sets the standalone mode of the analysis 
	 * @param standalone New configured mode of the analysis
	 */
	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}
	
	/**
	 * Returns, whether the analysis should run in standalone mode or not
	 * @return Returns true, if the analysis is in standalone mode. Otherwise, the method returns false
	 */
	public boolean isStandalone() {
		return standalone;
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
}
