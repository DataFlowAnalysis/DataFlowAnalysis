package org.dataflowanalysis.analysis.tests;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.result.DSLResult;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class DemoTest {
	protected PCMDataFlowConfidentialityAnalysis dataFlowAnalysis;

	@BeforeAll
	public void initializeDataFlowAnalysis() {
		final var usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel");
		final var allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation");
		final var nodeCharacteristicsPath = Paths.get("models", "BranchingOnlineShop", "default.nodecharacteristics");

		dataFlowAnalysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
				.modelProjectName(TEST_MODEL_PROJECT_NAME).usePluginActivator(Activator.class)
				.useUsageModel(usageModelPath.toString()).useAllocationModel(allocationPath.toString())
				.useNodeCharacteristicsModel(nodeCharacteristicsPath.toString()).build();
		dataFlowAnalysis.initializeAnalysis();
	}

	@Test
	public void testDataFlowAnalysisUsingManualQuerying() {
		PCMFlowGraphCollection flowGraphs = dataFlowAnalysis.findFlowGraphs();
		flowGraphs.evaluate();

		for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraphs.getTransposeFlowGraphs()) {
			List<? extends AbstractVertex<?>> violations = dataFlowAnalysis.queryDataFlow(transposeFlowGraph, node -> {

				List<String> serverLocation = node.getVertexCharacteristics("ServerLocation").stream()
						.map(CharacteristicValue::getValueName).toList();
				List<String> dataSensitivity = node.getDataCharacteristicMap("DataSensitivity").values().stream()
						.flatMap(Collection::stream).map(CharacteristicValue::getValueName).toList();

				return dataSensitivity.stream().anyMatch(l -> l.equals("Personal"))
						&& serverLocation.stream().anyMatch(l -> l.equals("nonEU"));
			});

			if (violations.size() > 0) {
				System.out.println("Confidentiality violations found!");
			}
		}
	}

	@Test
	public void testDataFlowAnalysisUsingTheDSL() {
		PCMFlowGraphCollection flowGraphs = dataFlowAnalysis.findFlowGraphs();
		flowGraphs.evaluate();

		var constraint = new ConstraintDSL().ofData().withLabel("DataSensitivity", List.of("Personal")).fromNode()
				.neverFlows().toVertex().withCharacteristic("ServerLocation", "nonEU").create();

		List<DSLResult> result = constraint.findViolations(flowGraphs);

		if (result.size() > 0) {
			System.out.println("Confidentiality violations found!");
		}
	}

}
