package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraph;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME; 

public class BaseTest {
	private DFDConfidentialityAnalysis analysis;
	
	@BeforeEach
	public void initAnalysis() {
		final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "branchingTest.dataflowdiagram");
		final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "branchingTest.datadictionary");
				
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
		DFDFlowGraph flowGraph = analysis.findFlowGraph();		
		assertEquals(flowGraph.getPartialFlowGraphs().size(), 4);
	}
	
	
	@Test
	public void noNodeCharacteristics_returnsNoViolation() {
		this.analysis.initializeAnalysis();
		DFDFlowGraph flowGraph = analysis.findFlowGraph();
		DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);
		
		var results = analysis.queryDataFlow(propagatedFlowGraph.getPartialFlowGraphs().get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void noNodeCharacteristics_returnsViolations() {
		this.analysis.initializeAnalysis();
		DFDFlowGraph flowGraph = analysis.findFlowGraph();
		DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);
		
		var results = analysis.queryDataFlow(propagatedFlowGraph.getPartialFlowGraphs().get(0), node -> {
    			return node.getAllNodeCharacteristics().size() != 0;
        }); 
		//results.forEach(res -> System.out.println(res.createPrintableNodeInformation()));
		assertTrue(!results.isEmpty());
	}
	
	@Test
	public void numberOfNodes_returnsNoViolation() {
		this.analysis.initializeAnalysis();
		DFDFlowGraph flowGraph = analysis.findFlowGraph();
		DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);
		
		var results = analysis.queryDataFlow(propagatedFlowGraph.getPartialFlowGraphs().get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
}