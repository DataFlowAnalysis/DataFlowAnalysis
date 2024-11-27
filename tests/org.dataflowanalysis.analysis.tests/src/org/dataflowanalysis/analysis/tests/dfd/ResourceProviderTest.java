package org.dataflowanalysis.analysis.tests.dfd;

import java.nio.file.Paths;
import java.nio.file.Path;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.Test;

public class ResourceProviderTest {
	 final Path minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
     final Path minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");
	
	@Test
	public void testCustomResourceProvider() {		
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
        
        var dd = dfduriResourceProvider.getDataDictionary();
        var dfd = dfduriResourceProvider.getDataFlowDiagram();
		        
		DFDModelResourceProvider dfdModelResourceProvider = new DFDModelResourceProvider(dd, dfd);
		
		var analysis = new DFDDataFlowAnalysisBuilder().standalone()
					.useCustomResourceProvider(dfdModelResourceProvider)
					.usePluginActivator(Activator.class)
					.modelProjectName(TEST_MODEL_PROJECT_NAME)
	                .build();
		
		analysis.initializeAnalysis();
		
		analysis.findFlowGraphs().evaluate();
	}
}
