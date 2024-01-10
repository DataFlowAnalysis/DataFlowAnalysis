package org.dataflowanalysis.analysis;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;

public abstract class DataFlowAnalysisBuilder {
	private final Logger logger = Logger.getLogger(DataFlowAnalysisBuilder.class);
	
	protected boolean standalone;
	protected String modelProjectName;
	protected Optional<Class<? extends Plugin>> pluginActivator;

	public DataFlowAnalysisBuilder() {
		this.pluginActivator = Optional.empty();
	}	
	
	/**
	 * Sets standalone mode of the analysis
	 * @return Builder of the analysis
	 */
	public DataFlowAnalysisBuilder standalone() {
		this.standalone = true;
		return this;
	}
	
	/**
	 * Sets the modelling project name of the analysis
	 * @return Builder of the analysis
	 */
	public DataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
		this.modelProjectName = modelProjectName;
		return this;
	}
	
	/**
	 * Sets the plugin activator project name of the analysis
	 * @return Builder of the analysis
	 */
	public DataFlowAnalysisBuilder usePluginActivator(Class<? extends Plugin> activator) {
		this.pluginActivator = Optional.of(activator);
		return this;
	}
	
	/**
	 * Validates the stored data
	 */
	protected void validate() {
		if (!this.standalone) {
			logger.error("The dataflow analysis can only be run in standalone mode", new IllegalStateException("Dataflow analysis can only be run in standalone mode"));
		}
		if (this.modelProjectName == null || this.modelProjectName.isEmpty()) {
			logger.error("The dataflow analysis reuqires a model project name to be present to resolve paths to the models", new IllegalStateException("Model project name is required"));
		}
	}

	/**
	 * Builds a new analysis from the given data
	 */
	public abstract DataFlowConfidentialityAnalysis build();
}
