package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;

public class DataFlowAnalysisBuilder extends AbstractDataFlowAnalysisBuilder<DataFlowConfidentialityAnalysis, AnalysisBuilderData>{

	public DataFlowAnalysisBuilder() {
		super(new AnalysisBuilderData());
		super.builder.add(this);
	}

	@Override
	public void checkBuilderData() {}

	@Override
	public DataFlowConfidentialityAnalysis build() {
		this.checkBuilderData();
		throw new IllegalStateException("No current implementation supports pcm-less analysis");
	}	
}
