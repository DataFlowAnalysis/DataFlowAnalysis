package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;;

public class DataFlowConfidentialityAnalysisBuilder extends AbstractDataFlowAnalysisBuilder<DataFlowConfidentialityAnalysis, AnalysisBuilderData> {
	
	/**
	 * Creates a new analysis builder with the given project name and project activator
	 * @param modelProjectName Name of the modeling project
	 * @param modelProjectActivator Activator plugin class of the modeling project
	 */
	public DataFlowConfidentialityAnalysisBuilder(String modelProjectName) {
		super(new AnalysisBuilderData());
		super.builderData.setModelProjectName(modelProjectName);
	}
	
	/**
	 * Sets the analysis mode to standalone
	 * @return Returns builder object of the analysis
	 */
	public DataFlowConfidentialityAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	public PCMDataFlowConfidentialityAnalysisBuilder pcm() {
		return new PCMDataFlowConfidentialityAnalysisBuilder(this);
	}
	
	/**
	 * Checks the provided data for issues or warnings
	 * @throws IllegalStateException The creation and execution with the current parameters is not permissible
	 */
	public void checkBuilderData() {
		if (!this.builderData.isStandalone()) {
			throw new IllegalStateException("Execution of the analysis is only supported in standalone mode");
		}
	}
	
	/**
	 * Create a new {@code DataFlowConfidentialityAnalysis} with the given parameter
	 * @return Returns a new analysis with the given parameter
	 */
	public DataFlowConfidentialityAnalysis build() {
		this.checkBuilderData();
		throw new IllegalStateException("Currently execution of the analysis only supports PCM! Please use the pcm() method to create one");
	}
}
