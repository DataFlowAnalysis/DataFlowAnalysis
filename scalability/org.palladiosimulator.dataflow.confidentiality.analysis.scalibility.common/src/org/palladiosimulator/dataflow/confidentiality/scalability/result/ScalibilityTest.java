package org.palladiosimulator.dataflow.confidentiality.scalability.result;

import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;

public abstract class ScalibilityTest {
	
	public abstract void run(ScalibilityParameter parameter, AnalysisExecutor analysisExecutor);
	
	public void runAnalysis(PCMModelFactory modelFactory, ScalibilityParameter scalibilityParameter, AnalysisExecutor analysisExecutor) {
		analysisExecutor.executeAnalysis(scalibilityParameter, modelFactory);
	}
	
	public abstract int getModelSize(int currentIndex);
	
	public abstract String getTestName();
}

/**
 * 
		scalibilityParameter.logAction("NewAnalysisExecution");
		StandalonePCMDataFlowConfidentialtyAnalysis analysis =
				new StandalonePCMDataFlowConfidentialtyAnalysis(AnalysisUtils.TEST_MODEL_PROJECT_NAME, 
						Activator.class, new PCMResourceListLoader(modelFactory.getResources()));
		analysis.initalizeAnalysis();
		scalibilityParameter.logAction("NewAnalysisInitializedAnalysis");
		List<ActionSequence> sequences = analysis.findAllSequences();
		scalibilityParameter.logAction("NewAnalysisSequences");
		analysis.evaluateDataFlows(sequences);
 */
