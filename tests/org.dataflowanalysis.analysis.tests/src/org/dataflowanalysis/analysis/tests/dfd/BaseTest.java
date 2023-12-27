package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME; 

public class BaseTest {
	private DFDConfidentialityAnalysis analysis;
	
	@BeforeEach
	public void initAnalysis() {
		final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "minimal.dataflowdiagram");
		final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "minimal.datadictionary");
		
		this.analysis = new DFDDataFlowAnalysisBuilder()
				.standalone()
				.modelProjectName(TEST_MODEL_PROJECT_NAME)
				.usePluginActivator(Activator.class)
				.useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
				.useDataDictionary(minimalDataDictionaryPath.toString())
				.build();
	}
	
	
	@Test
	public void numberOfSequences_equalsTwo() {
		this.analysis.initializeAnalysis();
		var sequences = analysis.findAllSequences();		
		assertEquals(sequences.size(), 2);
	}
	
	
	@Test
	public void noNodeCharacteristics_returnsNoViolation() {
		this.analysis.initializeAnalysis();
		var sequences = analysis.findAllSequences();
		List<ActionSequence> evaluatedSequences = this.analysis.evaluateDataFlows(sequences);
		
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void noNodeCharacteristics_returnsViolations() {
		this.analysis.initializeAnalysis();
		var sequences = analysis.findAllSequences();
		List<ActionSequence> evaluatedSequences = this.analysis.evaluateDataFlows(sequences);
		
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() != 0;
        }); 
		//results.forEach(res -> System.out.println(res.createPrintableNodeInformation()));
		assertTrue(!results.isEmpty());
	}
	
	@Test
	public void numberOfNodes_returnsNoViolation() {
		this.analysis.initializeAnalysis();
		var sequences = analysis.findAllSequences();
		List<ActionSequence> evaluatedSequences = this.analysis.evaluateDataFlows(sequences);
		
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
}