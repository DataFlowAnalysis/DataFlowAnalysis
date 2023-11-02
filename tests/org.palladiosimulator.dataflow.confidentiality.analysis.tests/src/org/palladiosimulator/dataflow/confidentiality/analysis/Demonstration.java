package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.DataFlowAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

@TestInstance(Lifecycle.PER_CLASS)
public class Demonstration {

	private DataFlowConfidentialityAnalysis analysis;

	@BeforeAll
	public void setupAnalysis() {
		var usageModelPath = Paths.get("models", "EncryptingOnlineShop", "default.usagemodel").toString();
		var allocationPath = Paths.get("models", "EncryptingOnlineShop", "default.allocation").toString();
		var characteristicsPath = Paths.get("models", "EncryptingOnlineShop", "default.nodecharacteristics").toString();

		analysis = new DataFlowAnalysisBuilder()
				.standalone()
				.modelProjectName(TEST_MODEL_PROJECT_NAME)
				.useBuilder(new PCMDataFlowConfidentialityAnalysisBuilder())
				.useNodeCharacteristicsModel(characteristicsPath)
				.usePluginActivator(Activator.class)
				.useUsageModel(usageModelPath)
				.useAllocationModel(allocationPath)
				.build();

		analysis.initializeAnalysis();
	}

	@Test
	public void runAnalysis() {
		var allSequences = analysis.findAllSequences();
		var propagationResult = analysis.evaluateDataFlows(allSequences);

		for (var sequence : propagationResult) {
			var violations = analysis.queryDataFlow(sequence, node -> {
				if (node.hasNodeCharacteristic("ServerLocation", "nonEU")) {
					return node.getAllDataFlowVariables().stream().anyMatch(v -> 
								v.hasDataCharacteristic("DataSensitivity", "Personal") &&
								!v.hasDataCharacteristic("Encryption", "Encrypted"));
				}
				return false;
			});

			System.out.println("Violations: " + violations);
		}
	}

}
