package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.palladiosimulator.dataflow.confidentiality.pcm.transformation.dcp.workflow.TransformPCMDFDWithConstraintsToPrologWorkflow;
import org.palladiosimulator.dataflow.confidentiality.pcm.transformation.dcp.workflow.TransformPCMDFDWithConstraintsToPrologWorkflowFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.transformation.dcp.workflow.jobs.TransformPCMDFDWithConstraintsToPrologJob;
import org.palladiosimulator.dataflow.confidentiality.pcm.transformation.dcp.workflow.jobs.TransformPCMDFDWithConstraintsToPrologJobBuilder;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.TransformPCMDFDToPrologWorkflowFactory;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.jobs.TransformPCMDFDToPrologJobBuilder;
import org.palladiosimulator.dataflow.confidentiality.pcm.workflow.jobs.TransformPCMDFDtoPrologJob;
import org.palladiosimulator.dataflow.confidentiality.scalability.AnalysisExecutor;
import org.palladiosimulator.dataflow.confidentiality.scalability.factory.PCMModelFactory;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityEvent;
import org.palladiosimulator.dataflow.confidentiality.scalability.result.ScalibilityParameter;

public class OldAnalysisExecutor implements AnalysisExecutor {

	@Override
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory) {
		scalibilityParameter.logAction(ScalibilityEvent.ANALYSIS_INITIALZATION);
		
		var job = TransformPCMDFDWithConstraintsToPrologJobBuilder.create()
			.addAllocationModel(modelFactory.getAllocation())
			.addUsageModels(modelFactory.getUsageModel())
			.addDCPDSL(createRelativePluginURI("/SEFFParameterTest/query.dcpdsl"))
			.setSerializeResultHandler(it -> System.out.println(it))
			.build();
		scalibilityParameter.logAction(ScalibilityEvent.SEQUENCE_FINDING);
		TransformPCMDFDWithConstraintsToPrologWorkflow workflow = TransformPCMDFDWithConstraintsToPrologWorkflowFactory.createWorkflow(job);
		workflow.run();
		scalibilityParameter.logAction(ScalibilityEvent.PROPAGATION);
	}

	@Override
	public String getPrefix() {
		return "Old";
	}
	
	private URI createRelativePluginURI(String relativePath) {
        String path = Paths.get("org.palladiosimulator.dataflow.confidentiality.analysis.scalibility.testmodels", relativePath)
            .toString();
        return URI.createPlatformPluginURI(path, false);
    }

}
