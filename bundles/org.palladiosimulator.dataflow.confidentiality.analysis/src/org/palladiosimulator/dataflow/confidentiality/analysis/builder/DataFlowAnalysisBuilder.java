package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;

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
	public void checkBuilderData() {
		if (!this.builderData.isStandalone()) {
			throw new IllegalStateException("Execution of the analysis is only supported in standalone mode");
		}
	}

	@Override
	public DataFlowConfidentialityAnalysis build() {
		this.checkBuilderData();
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
}
