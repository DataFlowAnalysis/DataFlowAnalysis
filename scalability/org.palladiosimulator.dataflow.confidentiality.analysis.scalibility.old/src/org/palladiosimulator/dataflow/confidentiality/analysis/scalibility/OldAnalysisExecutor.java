package org.palladiosimulator.dataflow.confidentiality.analysis.scalibility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import org.palladiosimulator.dataflow.confidentiality.transformation.dcp.workflow.internal.Activator;
import org.prolog4j.manager.IProverManager;

public class OldAnalysisExecutor implements AnalysisExecutor {

	@Override
	public void executeAnalysis(ScalibilityParameter scalibilityParameter, PCMModelFactory modelFactory) {
		scalibilityParameter.logAction(ScalibilityEvent.ANALYSIS_INITIALZATION);
						
//		var job = TransformPCMDFDWithConstraintsToPrologJobBuilder.create()
//			.addAllocationModel(modelFactory.getAllocation())
//			.addUsageModels(modelFactory.getUsageModel())
//			.addDCPDSL(queryURI)
//			.setSerializeResultHandler(it -> System.out.println(it))
//			.build();
		
		var job = TransformPCMDFDToPrologJobBuilder.create()
				.addAllocationModel(modelFactory.getAllocation())
				.addUsageModels(modelFactory.getUsageModel())
				.addSerializeModelToString()
				.build();
				
		
		scalibilityParameter.logAction(ScalibilityEvent.SEQUENCE_FINDING);
		var workflow = TransformPCMDFDToPrologWorkflowFactory.createWorkflow(job); 
		workflow.run();
		var prologCode = workflow.getPrologProgram().get();
		scalibilityParameter.logAction(ScalibilityEvent.PROPAGATION);
		var proverManager = Activator.getInstance().getProverManager().getProvers().values().iterator().next();
		var prover = proverManager.createProver();
		prover.addTheory(prologCode);
		File file = new File("/home/felix/Fluidtrust/Repositories/Palladio-Addons-DataFlowConfidentiality-Analysis/scalability/org.palladiosimulator.dataflow.confidentiality.analysis.scalibility.testmodels/" + scalibilityParameter.getTestName() + scalibilityParameter.getModelSize()  + ".pl");
		try {
			FileOutputStream out = new FileOutputStream(file);
			out.write(prologCode.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//var query = prover.solve("flowTree(N, PIN, S), nodeCharacteristic(N, NVAL, NTYP), characteristicTypeValue(NVAL, NTYP, NINDEX), characteristic(N, PIN, CVAL, CTYP, S), characteristicTypeValue(CVAL, TYP, CINDEX).");
		var query = prover.query("inputPin(N, PIN), flowTree(N, PIN, S), nodeCharacteristic(N, NVAL, NTYP), characteristicTypeValue(NVAL, NTYP, NINDEX), characteristic(N, PIN, CVAL, CTYP, S), characteristicTypeValue(CVAL, TYP, CINDEX).").solve();
		//var query = prover.solve("inputPin(N, PIN), flowTree(N, PIN, S), nodeCharacteristic(N, NVAL, NTYP), characteristicTypeValue(NVAL, NTYP, NINDEX), characteristic(N, PIN, CVAL, CTYP, S), characteristicTypeValue(CVAL, TYP, CINDEX), CINDEX=777, NINDEX=777.");
		var result = new ArrayList[] {new ArrayList<>(), new ArrayList<>()};
		query.collect(new String[] {"N", "PIN"}, result);
		if(result[0].size() < 1) {
			throw new RuntimeException(scalibilityParameter.getTestName() + ": Query failed");
		}
		for(int i = 0; i < result[0].size(); i++) {
			System.err.println(result[0].get(i) + ": " +  result[1].get(i));
		}
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
