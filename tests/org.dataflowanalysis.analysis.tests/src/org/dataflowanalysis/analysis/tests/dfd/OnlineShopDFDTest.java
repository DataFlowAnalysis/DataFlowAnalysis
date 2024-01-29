package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;

import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
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
		assertEquals(sequences.size(), 2);
	}

}
