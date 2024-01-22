package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.dfd.DFDConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.FlowGraph;

public class DFDMapperTest {
	
	private static String pathToDataDictionaryModel = "C:\\Users\\Huell\\Documents\\Studium\\HIWI\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\minimal.datadictionary";
	private static String pathToDFDModel = "C:\\Users\\Huell\\Documents\\Studium\\HIWI\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\minimal.dataflowdiagram";
	private static DFDConfidentialityAnalysis strandAnalysis;
	private static List<FlowGraph> evaluatedStrandSequences;
	
	@BeforeAll
	public static void setUp() {
		strandAnalysis = new DFDConfidentialityAnalysis(pathToDFDModel, pathToDataDictionaryModel);
		strandAnalysis.initializeAnalysis();
	}

	@Test
	public void numberOfStrandSequences_equalsTwo() {
		var strandSequences = strandAnalysis.findAllSequences();
		assertEquals(strandSequences.size(), 2);
	}
}
