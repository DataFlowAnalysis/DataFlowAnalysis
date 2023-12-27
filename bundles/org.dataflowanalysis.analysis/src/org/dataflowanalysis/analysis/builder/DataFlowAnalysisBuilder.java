package org.dataflowanalysis.analysis.builder;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;

public class DataFlowAnalysisBuilder {
	private AnalysisBuilderData builderData;

	public DataFlowAnalysisBuilder() {
		this.builderData = new AnalysisBuilderData();
	}	
	
	/**
	 * Sets standalone mode of the analysis
	 * @return Builder of the analysis
	 */
	public DataFlowAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	/**
	 * Sets the modelling project name of the analysis
	 * @return Builder of the analysis
	 */
	public DataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
		this.builderData.setModelProjectName(modelProjectName);
		return this;
	}

	public DataFlowConfidentialityAnalysis build() {
		builderData.validateData();
		throw new IllegalStateException("No current implementation supports pcm-less analysis");
	}
}
