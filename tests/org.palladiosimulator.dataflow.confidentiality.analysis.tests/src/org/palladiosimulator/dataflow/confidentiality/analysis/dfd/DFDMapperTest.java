package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd.DFDConfidentialityAnalysis;

public class DFDMapperTest {
	
	private static String pathToDataDictionaryModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\MinimalDataDictonairy.datadictionary"; //TODO
	private static String pathToStrandsDFDModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\DifferentStrands.dataflowdiagram";
	private static DFDConfidentialityAnalysis strandAnalysis;
	private static List<ActionSequence> evaluatedStrandSequences;
	
	@BeforeAll
	public static void setUp() {
		strandAnalysis = new DFDConfidentialityAnalysis(pathToStrandsDFDModel, pathToDataDictionaryModel);
		strandAnalysis.initializeAnalysis();
	}

	@Test
	public void numberOfStrandSequences_equalsThree() {
		var strandSequences = strandAnalysis.findAllSequences();
		assertEquals(strandSequences.size(), 3);
	}
}
