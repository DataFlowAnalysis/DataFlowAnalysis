package org.dataflowanalysis.analysis.tests.dfd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.nio.file.Paths;

import org.dataflowanalysis.analysis.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.utils.pcm.ResourceUtils;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import org.dataflowanalysis.analysis.testmodels.Activator;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME; 

public class BaseTest {
	private DFDConfidentialityAnalysis analysis;
	
	private static final String PLUGIN_PATH = "org.dataflowanalysis.analysis.tests";
	private ResourceSet resources = new ResourceSetImpl();


	@BeforeAll
	public static void setUpAnalysis() {
		initStandalone();
	}
	
	@BeforeEach
	public void initAnalysis() {
		final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "minimal.dataflowdiagram");
		final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "minimal.datadictionary");
        
		DataFlowDiagram dfd = (DataFlowDiagram) loadResource(ResourceUtils.createRelativePluginURI(minimalDataFlowDiagramPath.toString(),TEST_MODEL_PROJECT_NAME));
		DataDictionary dd = (DataDictionary) loadResource(ResourceUtils.createRelativePluginURI(minimalDataDictionaryPath.toString(),TEST_MODEL_PROJECT_NAME));
		
		this.analysis = new DFDConfidentialityAnalysis(dfd, dd);
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
	
    private static void initStandalone() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("dataflowdiagram", new XMIResourceFactoryImpl());
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("datadictionary", new XMIResourceFactoryImpl());
		
        try {
            StandaloneInitializerBuilder.builder()
                .registerProjectURI(BaseTest.class, PLUGIN_PATH)
                .registerProjectURI(Activator.class, TEST_MODEL_PROJECT_NAME)
                .build()
                .init();

        } catch (StandaloneInitializationException e) {
            e.printStackTrace();
        }
    }
    
	private EObject loadResource(URI modelURI) {
		Resource resource = this.resources.getResource(modelURI, true);
		if (resource == null) {
			throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded", modelURI));
		} else if (resource.getContents().isEmpty()) {
			throw new IllegalArgumentException(String.format("Model with URI %s is empty", modelURI));
		}
		return resource.getContents().get(0);
	}
}