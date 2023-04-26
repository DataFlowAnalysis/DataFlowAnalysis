package org.palladiosimulator.dataflow.confidentiality.scalability.result;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceListLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;

public abstract class ScalibilityTest {
	
	public abstract void run(ScalibilityParameter parameter);
	
	public void runNewAnalysis(PCMModelFactory modelFactory, ScalibilityParameter scalibilityParameter) {
		scalibilityParameter.logAction("NewAnalysisExecution");
		StandalonePCMDataFlowConfidentialtyAnalysis analysis =
				new StandalonePCMDataFlowConfidentialtyAnalysis(AnalysisUtils.TEST_MODEL_PROJECT_NAME, 
						Activator.class, new PCMResourceListLoader(modelFactory.getResources()));
		analysis.initalizeAnalysis();
		scalibilityParameter.logAction("NewAnalysisInitializedAnalysis");
		List<ActionSequence> sequences = analysis.findAllSequences();
		scalibilityParameter.logAction("NewAnalysisSequences");
		analysis.evaluateDataFlows(sequences);
	}
	
	public void runOldAnalysis(PCMModelFactory modelFactory, ScalibilityParameter scalibilityParameter) {
		// TODO: Start old analysis
	}
	
	public abstract int getModelSize(int currentIndex);
	
	public abstract String getTestName();
}
