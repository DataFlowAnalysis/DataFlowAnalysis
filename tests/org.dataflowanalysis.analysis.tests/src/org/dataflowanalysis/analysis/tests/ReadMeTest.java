package org.dataflowanalysis.analysis.tests;

import java.util.List;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
public class ReadMeTest extends BaseTest {
	
	// The following shows the code snippet used in the project README.md file.
	// Make all changes here and copy it over to the README afterward (and replace hard tabs in there).
	// Do *NOT* auto format this file!
	
	// --------------------------------------------------
	public static void main(String[] args) {
	    DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder()
	        .standalone()
	        .modelProjectName("<PROJECT_NAME>")
	        .usePluginActivator(Activator.class)
	        .useUsageModel("<USAGE_MODEL_PATH>")
	        .useAllocationModel("<ALLOCATION_MODEL_PATH>")
	        .useNodeCharacteristicsModel("<NODE_MODEL_PATH>")
	        .build();

	    analysis.setLoggerLevel(Level.TRACE); // Set desired logger level. Level.TRACE provides additional propagation Information
	    analysis.initializeAnalysis();

	    List<ActionSequence> actionSequences = analysis.findAllSequences();

	    List<ActionSequence> propagationResult = analysis.evaluateDataFlows(actionSequences);
	    
	    for(ActionSequence actionSequence : propagationResult) {
	    	List<AbstractActionSequenceElement<?>> violations = analysis.queryDataFlow(actionSequence,
	        it -> false // Constraint goes here, return true, if constraint is violated
	      );
	    }
	  }
	// --------------------------------------------------

	/**
	 * Tests, whether the code snippet from the README actually works using the travel planner analysis.
	 */
	@Test
	public void ReadmeWithTravelPlannerTest() {
		var analysis = travelPlannerAnalysis;
		
		// Code snippet from README starts here
		List<ActionSequence> actionSequences = analysis.findAllSequences();

	    List<ActionSequence> propagationResult = analysis.evaluateDataFlows(actionSequences);
	    
	    for(ActionSequence actionSequence : propagationResult) {
	    	List<AbstractActionSequenceElement<?>> violations = analysis.queryDataFlow(actionSequence,
	        it -> false // Constraint goes here, return true, if constraint is violated
	      );
	    }
	}
}
