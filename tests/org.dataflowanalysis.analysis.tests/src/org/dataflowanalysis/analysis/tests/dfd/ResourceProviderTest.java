package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLResourceFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResourceProviderTest {
    DataDictionary dataDictionary;
    DataFlowDiagram dataFlowDiagram;

    @BeforeEach
    public void setUp() {
        final Path minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
        final Path minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");
        final var minimalDataFlowDiagramPathDirect = Paths.get(TEST_MODEL_PROJECT_NAME, "models", "DFDTestModels", "BranchingTest.dataflowdiagram");
        final var minimalDataDictionaryPathDirect = Paths.get(TEST_MODEL_PROJECT_NAME, "models", "DFDTestModels", "BranchingTest.datadictionary");

        var dfdUri = URI.createPlatformPluginURI(minimalDataFlowDiagramPathDirect.toString(), false);
        var ddUri = URI.createPlatformPluginURI(minimalDataDictionaryPathDirect.toString(), false);

        var dummyToLoadPlugin = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .build();

        dummyToLoadPlugin.initializeAnalysis();

        DFDURIResourceProvider dfduriResourceProvider = new DFDURIResourceProvider(dfdUri, ddUri);
        dfduriResourceProvider.loadRequiredResources();

        this.dataDictionary = dfduriResourceProvider.getDataDictionary();
        this.dataFlowDiagram = dfduriResourceProvider.getDataFlowDiagram();
    }

    @Test
    public void testCustomResourceProvider() {
        DFDModelResourceProvider dfdModelResourceProvider = new DFDModelResourceProvider(this.dataDictionary, this.dataFlowDiagram);

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useCustomResourceProvider(dfdModelResourceProvider)
                .usePluginActivator(Activator.class)
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .build();

        analysis.initializeAnalysis();

        var flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();

        assertEquals(flowGraphs.getTransposeFlowGraphs()
                .size(), 4);
    }

    @Test
    public void testAbsolutePathForResourceProvider() {
        String tempDir = System.getProperty("java.io.tmpdir");
        var dfdFile = new File(tempDir, "BranchingTest.dataflowdiagram");
        var ddFile = new File(tempDir, "BranchingTest.datadictionary");
        dfdFile.deleteOnExit();
        ddFile.deleteOnExit();

        ResourceSet resourceSet = new ResourceSetImpl();
        Resource dfdResource = createAndAddResource(dfdFile.toString(), new String[] {"dataflowdiagram"}, resourceSet);
        Resource ddResource = createAndAddResource(ddFile.toString(), new String[] {"datadictionary"}, resourceSet);

        dfdResource.getContents()
                .add(this.dataFlowDiagram);
        ddResource.getContents()
                .add(this.dataDictionary);

        saveResource(dfdResource);
        saveResource(ddResource);

        var analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .useDataFlowDiagram(dfdFile.toString())
                .useDataDictionary(ddFile.toString())
                .build();

        analysis.initializeAnalysis();

        var flowGraphs = analysis.findFlowGraphs();
        flowGraphs.evaluate();

        assertEquals(flowGraphs.getTransposeFlowGraphs()
                .size(), 4);
    }

    private Resource createAndAddResource(String outputFile, String[] fileextensions, ResourceSet rs) {
        for (String fileext : fileextensions) {
            rs.getResourceFactoryRegistry()
                    .getExtensionToFactoryMap()
                    .put(fileext, new XMLResourceFactoryImpl());
        }
        URI uri = URI.createFileURI(outputFile);
        Resource resource = rs.createResource(uri);
        ((ResourceImpl) resource).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
        return resource;
    }

    private void saveResource(Resource resource) {
        Map<Object, Object> saveOptions = ((XMLResource) resource).getDefaultSaveOptions();
        saveOptions.put(XMLResource.OPTION_CONFIGURATION_CACHE, Boolean.TRUE);
        saveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
        try {
            resource.save(saveOptions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
