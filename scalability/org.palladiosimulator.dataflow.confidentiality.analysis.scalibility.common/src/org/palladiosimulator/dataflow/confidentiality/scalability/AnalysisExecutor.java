package org.palladiosimulator.dataflow.confidentiality.scalability;

import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public interface AnalysisExecutor {
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory);
	
	public String getPrefix();

}
