package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DFDCharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd.DFDConfidentialityAnalysis;

import mdpa.dfd.datadictionary.impl.datadictionaryFactoryImpl;

public class DFDCharacteristicCalculatorTest {

	private static String pathToDataDictionaryModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\MinimalDataDictonairy.datadictionary"; //TODO
	private static String pathToStrandsDFDModel = "F:\\EMF - Workspace\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\DifferentStrands.dataflowdiagram";
	private static DFDConfidentialityAnalysis analysis;
	private static List<ActionSequence> sequences;
	private static DataFlowVariable dataFlowVariable;
	
	@BeforeAll
	public static void setUp() {
		analysis = new DFDConfidentialityAnalysis(pathToStrandsDFDModel, pathToDataDictionaryModel);
		analysis.initializeAnalysis();
		
		sequences = analysis.findAllSequences();
		
		List<CharacteristicValue> characteristics = new ArrayList<CharacteristicValue> ();
		var ddFactory = datadictionaryFactoryImpl.init();
		var labelType = ddFactory.createLabelType();
		labelType.setEntityName("aName");
		labelType.setId("_tJZ2QEv-Ee6A36yDj2RUyQ");
		var label = ddFactory.createLabel();
		label.setEntityName("aName");
		label.setId("_uCCHYEv-Ee6A36yDj2RUyQ");
		characteristics.add(new DFDCharacteristicValue(labelType, label));
		dataFlowVariable = new DataFlowVariable("aName", characteristics);
	}
	
	
	@Test
	public void fillDataFlowVariables_fillsDataFlowsWithLabel() {
		var evaluatedSequences = analysis.evaluateDataFlows(sequences);
		assertEquals(evaluatedSequences.get(0).getElements().get(0).getAllDataFlowVariables().get(0), dataFlowVariable);
		
	}
}
