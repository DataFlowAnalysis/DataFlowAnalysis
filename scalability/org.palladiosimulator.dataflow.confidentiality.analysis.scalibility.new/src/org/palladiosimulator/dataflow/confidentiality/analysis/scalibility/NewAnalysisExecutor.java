package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.DataFlowAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public class NewAnalysisExecutor implements AnalysisExecutor {
	private Logger logger = Logger.getLogger(NewAnalysisExecutor.class);
	
	@Override
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory) {
		scalibilityParameter.logAction(ScalibilityEvent.ANALYSIS_INITIALZATION);
		DataFlowConfidentialityAnalysis analysis = new DataFlowAnalysisBuilder()
				.standalone()
				.modelProjectName(AnalysisUtils.TEST_MODEL_PROJECT_NAME)
				.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
				.usePluginActivator(Activator.class)
				.useResources(modelFactory.getResources())
				.build();
		analysis.initalizeAnalysis();
		scalibilityParameter.logAction(ScalibilityEvent.SEQUENCE_FINDING);
		List<ActionSequence> sequences = analysis.findAllSequences();
		scalibilityParameter.logAction(ScalibilityEvent.PROPAGATION);
		analysis.evaluateDataFlows(sequences);
		var result = sequences.stream()
				.map(it -> analysis.queryDataFlow(it, (seq) -> true))
				.flatMap(it -> it.stream())
				.collect(Collectors.toList());
		logger.info("Found " + result.size() + " violations");
	}

	@Override
	public String getPrefix() {
		return "New";
	}

}
