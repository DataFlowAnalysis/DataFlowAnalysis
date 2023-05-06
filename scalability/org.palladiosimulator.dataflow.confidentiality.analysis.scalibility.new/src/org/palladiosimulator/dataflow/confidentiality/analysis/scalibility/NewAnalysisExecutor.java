package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceListLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public class NewAnalysisExecutor implements AnalysisExecutor {

	@Override
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory) {
		scalibilityParameter.logAction(ScalibilityEvent.ANALYSIS_INITIALZATION);
		StandalonePCMDataFlowConfidentialtyAnalysis analysis =
				new StandalonePCMDataFlowConfidentialtyAnalysis(AnalysisUtils.TEST_MODEL_PROJECT_NAME, 
						Activator.class, new PCMResourceListLoader(modelFactory.getResources()));
		analysis.initalizeAnalysis();
		scalibilityParameter.logAction(ScalibilityEvent.SEQUENCE_FINDING);
		List<ActionSequence> sequences = analysis.findAllSequences();
		scalibilityParameter.logAction(ScalibilityEvent.PROPAGATION);
		analysis.evaluateDataFlows(sequences);
	}

	@Override
	public String getPrefix() {
		return "New";
	}

}
