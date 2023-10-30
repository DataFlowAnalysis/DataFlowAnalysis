package org.dataflowanalysis.analysis.builder;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;

public class DataFlowAnalysisBuilder extends AbstractDataFlowAnalysisBuilder<DataFlowConfidentialityAnalysis, AnalysisBuilderData, AnalysisBuilderData>{

	public DataFlowAnalysisBuilder() {
		super(new AnalysisBuilderData());
		super.builder.add(this);
	}
	
	@Override
	public void copyBuilderData(AnalysisBuilderData builderData) {
		super.builderData.setModelProjectName(builderData.getModelProjectName());
		super.builderData.setStandalone(builderData.isStandalone());
	}	

	@Override
	public void validateBuilderData() {
		this.builderData.validateData();
	}

	@Override
	public DataFlowConfidentialityAnalysis build() {
		this.validateBuilderData();
		throw new IllegalStateException("No current implementation supports pcm-less analysis");
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
}
