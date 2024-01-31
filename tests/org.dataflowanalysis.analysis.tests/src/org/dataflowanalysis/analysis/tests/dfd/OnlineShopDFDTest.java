package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.file.Paths;
import java.util.List;

import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDActionSequenceElement;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OnlineShopDFDTest {

	private DFDConfidentialityAnalysis analysis;

	@BeforeEach
	public void initAnalysis() {
		final var dataFlowDiagramPath = Paths.get("models", "OnlineShopDFDsimple", "onlineshop.dataflowdiagram");
		final var dataDictionaryPath = Paths.get("models", "OnlineShopDFDsimple", "onlineshop.datadictionary");

		this.analysis = new DFDDataFlowAnalysisBuilder().standalone().modelProjectName(TEST_MODEL_PROJECT_NAME)
				.usePluginActivator(Activator.class).useDataFlowDiagram(dataFlowDiagramPath.toString())
				.useDataDictionary(dataDictionaryPath.toString()).build();

		this.analysis.initializeAnalysis();
	}

	@Test
	public void numberOfSequences_equalsTwo() {
		var sequences = analysis.findAllSequences();
		assertEquals(sequences.size(), 3);
	}

	@Test
	public void checkFirstSequenceEntries() {
		var sequences = analysis.findAllSequences();
		var entityNames = sequences.get(0).getElements().stream().map(DFDActionSequenceElement.class::cast)
				.map(DFDActionSequenceElement::getName).toList();

		var expectedNames = List.of("UserRequesting", "view", "DatabaseReceiving");
		assertIterableEquals(expectedNames, entityNames);
	}

	@Test
	public void testNodeLabels() {
		var sequences = analysis.evaluateDataFlows(analysis.findAllSequences());
		var userVertex = (DFDActionSequenceElement) sequences.get(0).getElements().get(0);
		var userVertexLabels = retrieveNodeLabels(userVertex);

		var expectedLabels = List.of("EU");
		assertIterableEquals(expectedLabels, userVertexLabels);
	}

	@Test
	public void testDataLabelPropagation() {
		var sequences = analysis.evaluateDataFlows(analysis.findAllSequences());
		var databaseVertex = (DFDActionSequenceElement) sequences.get(2).getElements().get(4);
		assertEquals("DatabaseReceiving", databaseVertex.getName());

		var propagatedLabels = retrieveDataLabels(databaseVertex);

		var expectedPropagatedLables = List.of("Personal", "Encrypted");
		assertIterableEquals(expectedPropagatedLables, propagatedLabels);
	}

	@Test
	public void testRealisticConstraints() {
		var sequences = analysis.evaluateDataFlows(analysis.findAllSequences());

		// Constraint 1: Personal data flowing to a node that is deployed outside the EU
		// Should find 1 violation
		int violationsFound = 0;
		for (var actionSequence : sequences) {
			var violations = analysis.queryDataFlow(actionSequence, it -> {
				var nodeLabels = retrieveNodeLabels(it);
				var dataLabels = retrieveDataLabels(it);

				return nodeLabels.contains("nonEU") && dataLabels.contains("Personal");
			});

			violationsFound += violations.size();
		}
		assertEquals(1, violationsFound);

		// Constraint 2: Personal data in a node deployed outside the EU w/o encryption
		// Should find 0 violations
		for (var actionSequence : sequences) {
			var violations = analysis.queryDataFlow(actionSequence, it -> {
				var nodeLabels = retrieveNodeLabels(it);
				var dataLabels = retrieveDataLabels(it);

				return nodeLabels.contains("nonEU") && dataLabels.contains("Personal")
						&& !dataLabels.contains("Encrypted");
			});

			assertEquals(0, violations.size());
		}
	}

	private List<String> retrieveNodeLabels(AbstractActionSequenceElement<?> vertex) {
		return vertex.getAllNodeCharacteristics().stream().map(DFDCharacteristicValue.class::cast)
				.map(DFDCharacteristicValue::getValueName).toList();
	}

	private List<String> retrieveDataLabels(AbstractActionSequenceElement<?> vertex) {
		return vertex.getAllDataFlowVariables().stream().map(DataFlowVariable::getAllCharacteristics)
				.flatMap(List::stream).map(DFDCharacteristicValue.class::cast).map(DFDCharacteristicValue::getValueName)
				.toList();
	}
}
