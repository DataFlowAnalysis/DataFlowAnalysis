package org.dataflowanalysis.analysis.tests.dfd;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.DFDDataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraph;
import org.dataflowanalysis.analysis.dfd.core.DFDVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BaseTest {
  private DFDConfidentialityAnalysis analysis;

  @BeforeEach
  public void initAnalysis() {
    final var minimalDataFlowDiagramPath =
        Paths.get("models", "DFDTestModels", "BranchingTest.dataflowdiagram");
    final var minimalDataDictionaryPath =
        Paths.get("models", "DFDTestModels", "BranchingTest.datadictionary");

    this.analysis =
        new DFDDataFlowAnalysisBuilder()
            .standalone()
            .modelProjectName(TEST_MODEL_PROJECT_NAME)
            .usePluginActivator(Activator.class)
            .useDataFlowDiagram(minimalDataFlowDiagramPath.toString())
            .useDataDictionary(minimalDataDictionaryPath.toString())
            .build();
  }

  @Test
  public void numberOfPartialFlowGraphs_equalsFour() {
    this.analysis.initializeAnalysis();
    DFDFlowGraph flowGraph = analysis.findFlowGraph();
    assertEquals(flowGraph.getPartialFlowGraphs().size(), 4);
  }

  @Test
  public void noVertexCharacteristics_returnsNoViolation() {
    this.analysis.initializeAnalysis();
    DFDFlowGraph flowGraph = analysis.findFlowGraph();
    DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

    var results =
        analysis.queryDataFlow(
            propagatedFlowGraph.getPartialFlowGraphs().get(0),
            node -> {
              return node.getAllNodeCharacteristics().size() == 0;
            });
    assertTrue(results.isEmpty());
  }

  @Test
  public void noVertexCharacteristics_returnsViolations() {
    this.analysis.initializeAnalysis();
    DFDFlowGraph flowGraph = analysis.findFlowGraph();
    DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

    var results =
        analysis.queryDataFlow(
            propagatedFlowGraph.getPartialFlowGraphs().get(0),
            node -> {
              return node.getAllNodeCharacteristics().size() != 0;
            });
    assertTrue(!results.isEmpty());
  }

  @Test
  public void numberOfNodes_returnsNoViolation() {
    this.analysis.initializeAnalysis();
    DFDFlowGraph flowGraph = analysis.findFlowGraph();
    DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

    var results =
        analysis.queryDataFlow(
            propagatedFlowGraph.getPartialFlowGraphs().get(0),
            node -> {
              return node.getAllNodeCharacteristics().size() == 0;
            });
    assertTrue(results.isEmpty());
  }
  
  @Test
  public void test_unification() {
	  this.analysis.initializeAnalysis();
	    DFDFlowGraph flowGraph = analysis.findFlowGraph();
	    DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

	    for (var pfg : propagatedFlowGraph.getPartialFlowGraphs()) {
	    	assertTrue(pfg.getVertices().stream().filter(v -> ((DFDVertex)v).getName().equals("In")).count() <  2);
	    }
  }
  
  @Test
  public void test_labelPropagation() {
	  this.analysis.initializeAnalysis();
	    DFDFlowGraph flowGraph = analysis.findFlowGraph();
	    DFDFlowGraph propagatedFlowGraph = this.analysis.evaluateFlowGraph(flowGraph);

	    for (var pfg : propagatedFlowGraph.getPartialFlowGraphs()) {
	    	for (var vertex : pfg.getVertices()) {
	    		assertTrue((!vertex.getAllIncomingDataFlowVariables().isEmpty()) || (!vertex.getAllOutgoingDataFlowVariables().isEmpty())); 
	    	}
	    }
  }

}
