package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;

public abstract class AbsrtractDataFlowConfidentialityAnalysisBuilder<T extends DataFlowConfidentialityAnalysis, D extends AnalysisBuilderData> {
	protected final Logger logger = Logger.getLogger(DataFlowConfidentialityAnalysisBuilder.class);
	protected final D builderData;

	
	public AbsrtractDataFlowConfidentialityAnalysisBuilder(D builderData) {
		this.builderData = builderData;
	}
	
	public abstract T build();
	
	public abstract void checkBuilderData();
	
	public D getBuilderData() {
		return this.builderData;
	}
}
