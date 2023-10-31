package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

public class AnalysisBuilderData {
	protected boolean standalone;
	protected String modelProjectName;
	
	public AnalysisBuilderData() {
		
	}
	
	/**
	 * Creates the analysis builder data object with the given data
	 * @param other Data which will be copied
	 */
	public AnalysisBuilderData(AnalysisBuilderData other) {
		this.modelProjectName = other.getModelProjectName();
		this.standalone = other.isStandalone();
	}
	
	/**
	 * Validates the saved Data
	 * @throws IllegalStateException Validation has failed
	 */
	public void validateData() {
		if (!this.isStandalone()) {
			throw new IllegalStateException("Execution of the analysis is currently only supported in standalone mode");
		}
		if(this.getModelProjectName() == null || this.getModelProjectName().isBlank()) {
			throw new IllegalStateException("A name for the modelling project is required!");
		}
	}
	
	
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
}
