package org.dataflowanalysis.analysis.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.dataflowanalysis.analysis.builder.validation.ValidationError;
import org.dataflowanalysis.analysis.builder.validation.ValidationErrorSeverity;
import org.dataflowanalysis.analysis.builder.validation.ValidationObject;
import org.eclipse.core.runtime.Plugin;

public class AnalysisBuilderData implements ValidationObject {
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

	@Override
	public List<ValidationError> validate() {
		List<ValidationError> validationErrors = new ArrayList<>();
		if (!this.isStandalone()) {
			validationErrors.add(new ValidationError("Execution of the analysis is currently only supported in standalone mode", ValidationErrorSeverity.ERROR));
		}
		if(this.getModelProjectName() == null || this.getModelProjectName().isBlank()) {
			validationErrors.add(new ValidationError("A name for the modelling project is required!", ValidationErrorSeverity.ERROR));
		}
		return validationErrors;
	}
}
