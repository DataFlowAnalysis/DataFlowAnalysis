package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.TransformPCMDFDToPrologWorkflowFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.jobs.TransformPCMDFDToPrologJobBuilder;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public class OldAnalysisExecutor implements AnalysisExecutor {

	@Override
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory) {
		scalibilityParameter.logAction("AnalysisExecution");
		var job = TransformPCMDFDToPrologJobBuilder.create()
			.addAllocationModel(modelFactory.getAllocation())
			.addUsageModels(modelFactory.getUsageModel())
			.addSerializeModelToFile(URI.createFileURI("../prolog-out"))
			.build();
		scalibilityParameter.logAction("AnalysisInitializedAnalysis");
		var workflow = TransformPCMDFDToPrologWorkflowFactory.createWorkflow(job);
		scalibilityParameter.logAction("AnalysisSequences");
		
	}

	@Override
	public String getPrefix() {
		return "Old";
	}

}
