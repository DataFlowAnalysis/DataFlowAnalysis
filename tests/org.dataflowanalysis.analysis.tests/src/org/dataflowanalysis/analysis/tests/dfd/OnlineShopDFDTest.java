package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.nio.file.Paths;
import java.util.List;

import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDActionSequenceElement;
import org.dataflowanalysis.analysis.dfd.core.DFDCharacteristicValue;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
		assertEquals(sequences.size(), 2);
	}

	@Test
	public void checkFirstSequenceEntries() {
		var sequences = analysis.findAllSequences();
		var entityNames = sequences.get(0).getElements().stream().map(DFDActionSequenceElement.class::cast)
				.map(DFDActionSequenceElement::getName).toList();

		var expectedNames = List.of("UserRequesting", "view", "Database", "display", "UserReceiving");
		assertIterableEquals(expectedNames, entityNames);
	}

	@Test
	public void testNodeLabels() {
		var sequences = analysis.evaluateDataFlows(analysis.findAllSequences());
		var userVertex = (DFDActionSequenceElement) sequences.get(0).getElements().get(0);
		var userVertexLabels = userVertex.getAllNodeCharacteristics().stream().map(DFDCharacteristicValue.class::cast)
				.map(DFDCharacteristicValue::getValueName).toList();

		var expectedLabels = List.of("EU");
		assertIterableEquals(expectedLabels, userVertexLabels);
	}

	// TODO: Re-enable after clarification of label propagation
	@Test @Disabled
	public void testDataLabelPropagation() {
		var sequences = analysis.evaluateDataFlows(analysis.findAllSequences());
		var databaseVertex = (DFDActionSequenceElement) sequences.get(1).getElements().get(4);
		assertEquals("Database", databaseVertex.getName());

		var propagatedLabels = databaseVertex.getAllDataFlowVariables().stream()
				.map(DataFlowVariable::getAllCharacteristics).flatMap(List::stream)
				.map(DFDCharacteristicValue.class::cast).map(DFDCharacteristicValue::getValueName).toList();

		var expectedPropagatedLables = List.of("Personal", "Encrypted");
		assertIterableEquals(expectedPropagatedLables, propagatedLabels);
	}

	@Test
	public void testRealisticConstraint() {

	}

}
