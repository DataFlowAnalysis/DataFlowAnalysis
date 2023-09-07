package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd.DFDConfidentialityAnalysis;

public class BaseTest {
	private static String pathToDFDModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\minimal.dataflowdiagram"; //TODO
	private static String pathToDataDictionaryModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\MinimalDataDictonairy.datadictionary"; //TODO
	private static String pathToStrandsDFDModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\DifferentStrands.dataflowdiagram";
	private static List<ActionSequence> evaluatedSequences;
	private static DFDConfidentialityAnalysis analysis;
	private static DFDConfidentialityAnalysis strandAnalysis;
	private static List<ActionSequence> evaluatedStrandSequences;

	@BeforeAll
	public static void setUpAnalysis() {
		analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();

		var sequences = analysis.findAllSequences();
		evaluatedSequences = analysis.evaluateDataFlows(sequences);
		
		strandAnalysis = new DFDConfidentialityAnalysis(pathToStrandsDFDModel, pathToDataDictionaryModel);
		strandAnalysis.initializeAnalysis();
		var strandSequences = strandAnalysis.findAllSequences();
		evaluatedStrandSequences = strandAnalysis.evaluateDataFlows(strandSequences);
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
		analysis.findAllSequences();
	}
	
	
	@Test
	public void evaluateDataFlows_throwsNothing() {
		var analysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();
		var sequences = analysis.findAllSequences();
		analysis.evaluateDataFlows(sequences);
	}
	
	@Test
	public void numberOfSequences_equalsTwo() {
		assertEquals(evaluatedSequences.size(), 2);
	}
	
	@Test
	public void numberOfStrandSequences_equalsThree() {
		assertEquals(evaluatedStrandSequences.size(), 3);
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
