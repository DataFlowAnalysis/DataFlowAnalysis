package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiatlity.analysis.dfd.DFDLoader;

public class DFDLoaderTest {
	private static String pathToDataDictionaryModel = "Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\MinimalDataDictonairy.datadictionary";
	private static String pathToStrandsDFDModel = "Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.tests\\src\\org\\palladiosimulator\\dataflow\\confidentiality\\analysis\\dfd\\DifferentStrands.dataflowdiagram";
	
	@Test
	public void loadStrandsDFDModel_returnsRightNumberOfElements() {
		var dfd = DFDLoader.loadDFDModel(pathToStrandsDFDModel);
		assertEquals(dfd.getFlows().size(), 4);
		assertEquals(dfd.getNodes().size(), 6);
	}
	
	@Test
	public void loadDataDictionary_returnsRightNumberOfElements() {
		var dd = DFDLoader.loadDataDictionaryModel(pathToDataDictionaryModel);
		assertEquals(dd.getBehaviour().size(), 1);
		assertEquals(dd.getLabelTypes().size(), 1);
	}

}
