package org.palladiosimulator.dataflow.confidentiality.analysis.dfd;

import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.dfd.DFDLoader;

public class DFDLoaderTest {
	private static String pathToDataDictionaryModel = "C:\\Users\\Huell\\Documents\\Studium\\HIWI\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\minimal.datadictionary";
	private static String pathToDFDModel = "C:\\Users\\Huell\\Documents\\Studium\\HIWI\\Palladio-Addons-DataFlowConfidentiality-Analysis\\tests\\org.palladiosimulator.dataflow.confidentiality.analysis.testmodels\\models\\DFDTestModels\\minimal.dataflowdiagram";
	
	@Test
	public void loadStrandsDFDModel_returnsRightNumberOfElements() {
		var dfd = DFDLoader.loadDFDModel(pathToDFDModel);
		assertEquals(dfd.getFlows().size(), 4);
		assertEquals(dfd.getNodes().size(), 5);
	}
	
	@Test
	public void loadDataDictionary_returnsRightNumberOfElements() {
		var dd = DFDLoader.loadDataDictionaryModel(pathToDataDictionaryModel);
		assertEquals(dd.getBehaviour().size(), 5);
		assertEquals(dd.getLabelTypes().size(), 1);
	}

}
