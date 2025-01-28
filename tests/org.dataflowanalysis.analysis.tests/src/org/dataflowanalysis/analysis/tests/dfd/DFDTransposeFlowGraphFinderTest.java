package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;

import java.nio.file.Paths;

import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.Test;

public class DFDTransposeFlowGraphFinderTest {
	DFDConfidentialityAnalysis analysis;	
	
	/**
	 * Tests equivalence of copied TFGs
	 */
	@Test
	public void testTFGCopy() {
		final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
        final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");

        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .build();
        
        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs().getTransposeFlowGraphs().get(0);
        
        var copiedFlowGraph = flowGraph.copy();
        
        assert(copiedFlowGraph.getVertices().stream().allMatch(copiedVertex -> {
        	return flowGraph.getVertices().stream().anyMatch(vertex -> vertex.equals(copiedVertex));
        }));
	}
	
	/**
	 * Tests correct detection of cyclic DFDs
	 */
	@Test
	public void testCyclicDetection() {
		final var loopDataFlowDiagramPath = Paths.get("models", "simpleLoopDFD", "loopDFD.dataflowdiagram");
        final var loopDataDictionaryPath = Paths.get("models", "simpleLoopDFD", "loopDFD.datadictionary");

        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(loopDataFlowDiagramPath.toString())
                .useDataDictionary(loopDataDictionaryPath.toString())
                .build();
        
        analysis.initializeAnalysis();
        assert(analysis.findFlowGraphs().wasCyclic());
        
        final var minimalDataFlowDiagramPath = Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
        final var minimalDataDictionaryPath = Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");

        this.analysis = new DFDDataFlowAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME)
                .usePluginActivator(Activator.class)
                .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
                .useDataDictionary(minimalDataDictionaryPath.toString())
                .build();
        
        analysis.initializeAnalysis();
        assert(!analysis.findFlowGraphs().wasCyclic());
	}
}
