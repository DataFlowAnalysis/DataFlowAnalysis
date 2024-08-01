package org.dataflowanalysis.analysis.tests.constraint;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.PCMCharacteristicValue;
import org.dataflowanalysis.examplemodels.Activator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CWA_constraintTest {
	
	 private DataFlowConfidentialityAnalysis analysis;
	 
	 	@Test
	    @DisplayName("Test Original CWA")
	    public void testBaseScenario() {
	 		testRealisticConstraints("CoronaWarnApp", "default", TEST_MODEL_PROJECT_NAME);
	    }
		
		@Test
	    @DisplayName("Test Scenario 1 CWA")
	    public void testScenarioOne() {
			testRealisticConstraints("CoronaWarnApp_UncertaintyScenario1", "default", TEST_MODEL_PROJECT_NAME);
	    }
		
		@Test
	    @DisplayName("Test Scenario 2 CWA")
	    public void testScenarioTwo() {
			testRealisticConstraints("CoronaWarnApp_UncertaintyScenario2", "default", TEST_MODEL_PROJECT_NAME);
	    }
		
		@Test
	    @DisplayName("Test Scenario 3 CWA")
	    public void testScenarioThree() {
			testRealisticConstraints("CoronaWarnApp_UncertaintyScenario3", "default", TEST_MODEL_PROJECT_NAME);
	    }
		
		@Test
	    @DisplayName("Test Scenario 4 CWA")
	    public void testScenarioFour() {
			testRealisticConstraints("CoronaWarnApp_UncertaintyScenario4", "default", TEST_MODEL_PROJECT_NAME);
	    }

		
		
		 public void testRealisticConstraints(String inputModel, String inputFile, String modelLocation) {
			 final var usageModelPath = Paths.get("casestudies", inputModel, inputFile + ".usagemodel")
		                .toString();
		        final var allocationPath = Paths.get("casestudies", inputModel, inputFile + ".allocation")
		                .toString();
		        final var nodeCharPath = Paths.get("casestudies", inputModel, inputFile + ".nodecharacteristics")
		                .toString();
	        
	        analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
	                .modelProjectName(TEST_MODEL_PROJECT_NAME)
	                .usePluginActivator(Activator.class)
	                .useUsageModel(usageModelPath)
	                .useAllocationModel(allocationPath)
	                .useNodeCharacteristicsModel(nodeCharPath)
	                .build();	
	        
	        analysis.initializeAnalysis();
	        var flowGraph = analysis.findFlowGraphs();
	        flowGraph.evaluate();

	        // Scenario 1, Communication Intercepted

	        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
	            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
	                var nodeLabels = retrieveNodeLabels(it);
	                var dataLabels = retrieveDataLabels(it);

	                return (dataLabels.contains("Sensitive") && dataLabels.contains("ConnectionIntercepted")) &&
	                		nodeLabels.contains("IllegalDeploymentLocation");
	            });
	            assertEquals(0, violations.size());
	        }	
	        
	     // Scenario 2, ConfidentialDataNotExpected

	        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
	            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
	                var dataLabels = retrieveDataLabels(it);
	                var nodeLabels = retrieveNodeLabels(it);

	                return (dataLabels.contains("ConfidentialDataNotExpected")) &&
	                		nodeLabels.contains("OpenTelekomCloud");
	            });
	            assertEquals(0, violations.size());
	        }	

	        // Scenario 3, ValidationFailure

	        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
	            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
	                var dataLabels = retrieveDataLabels(it);

	                return (dataLabels.contains("ValidationFailed") && ((AbstractPCMVertex<?>)it).getReferencedElement().getEntityName().equals("StoreKeys"));
	            });
	            assertEquals(0, violations.size());
	        }
	        
	        // Scenario 4, KeyIssue

	        for (var transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
	            var violations = analysis.queryDataFlow(transposeFlowGraph, it -> {
	                var dataLabels = retrieveDataLabels(it);

	                return (dataLabels.contains("KeyIssue") && dataLabels.contains("RetrievedConfidentialDetails"));
	            });
	            assertEquals(0, violations.size());
	        }
	    }

	
	private List<String> retrieveNodeLabels(AbstractVertex<?> vertex) {
        return vertex.getAllVertexCharacteristics()
                .stream()
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getValueName)
                .toList();
    }

    private List<String> retrieveDataLabels(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getValueName)
                .toList();
    }
    
    private List<String> retrieveDataLabelsTypes(AbstractVertex<?> vertex) {
        return vertex.getAllDataCharacteristics()
                .stream()
                .map(DataCharacteristic::getAllCharacteristics)
                .flatMap(List::stream)
                .map(PCMCharacteristicValue.class::cast)
                .map(PCMCharacteristicValue::getTypeName)
                .toList();
    }
}
