package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.nio.file.Paths;

import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

public class BaseTest {
	private static String pathToDFDModel;
	private static String pathToDataDictionaryModel;
	private static List<FlowGraph> evaluatedSequences;
	private static DFDConfidentialityAnalysis analysis;

	@BeforeAll
	public static void setUpAnalysis() {
		//pathToDFDModel = Paths.get(TEST_MODEL_PROJECT_NAME, "models", "DFDTestModels", "minimal.dataflowdiagram").toString();
		//pathToDataDictionaryModel = Paths.get(TEST_MODEL_PROJECT_NAME, "models", "DFDTestModels", "minimal.datadictionary").toString();
		pathToDataDictionaryModel = "C:\\Users\\Huell\\Documents\\Studium\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\BranchingTest.datadictionary";
		pathToDFDModel = "C:\\Users\\Huell\\Documents\\Studium\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\BranchingTest.dataflowdiagram";
		analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();

		var sequences = analysis.findAllFlowGraphs();
		var evaluatedSequences = analysis.evaluateDataFlows(sequences);
		System.out.print(evaluatedSequences.size());
	}
	
	
	@Test
	public void initializeAnalysis_throwsNothing() {
		var analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();
	}
	
	@Test
	public void findAllSequences_throwsNothing() {
		var analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();
		analysis.findAllFlowGraphs();
	}
	
	
	@Test
	public void evaluateDataFlows_throwsNothing() {
		var analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();
		var sequences = analysis.findAllFlowGraphs();
		analysis.evaluateDataFlows(sequences);
	}
	
	@Test
	public void numberOfSequences_equalsTwo() {
		assertEquals(evaluatedSequences.size(), 2);
	}
	
	
	@Test
	public void noNodeCharacteristics_returnsNoViolation() {
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void noNodeCharacteristics_returnsViolations() {
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() != 0;
        });
		System.out.println(results.get(0).createPrintableNodeInformation());
		assertTrue(!results.isEmpty());
	}
	
	@Test
	public void numberOfNodes_returnsNoViolation() {
		var results = analysis.queryDataFlow(evaluatedSequences.get(0), node -> {
    			return node.getAllNodeCharacteristics().size() == 0;
        });
		assertTrue(results.isEmpty());
	}
	
}
