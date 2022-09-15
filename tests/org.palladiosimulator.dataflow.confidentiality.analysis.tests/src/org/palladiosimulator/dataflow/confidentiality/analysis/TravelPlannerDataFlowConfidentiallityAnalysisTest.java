package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertUserSequenceElementContent;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

public class TravelPlannerDataFlowConfidentiallityAnalysisTest {
	
	private static StandalonePCMDataFlowConfidentialtyAnalysis analysis;
	
	/**
	 * Initializes the analysis with the TravelPlanner test model and initializes the Analysis
	 */
	@BeforeAll
	public static void setupAnalysis() {
        final var usageModelPath = Paths.get("models", "TravelPlanner", "travelPlanner.usagemodel").toString();
		final var allocationPath = Paths.get("models", "TravelPlanner", "travelPlanner.allocation").toString();

        analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(
                TEST_MODEL_PROJECT_NAME, Activator.class, usageModelPath, allocationPath);

        analysis.initalizeAnalysis();
	}
	
	/**
	 * Tests the standalone analysis by executing the ActionSequenceFinder. Fails if no sequences can be found
	 */
	@Test
	public void testStandaloneAnalysis() {
        var allSequences = analysis.findAllSequences();
        allSequences.stream()
            .map(it -> it.toString())
            .forEach(System.out::println);

        assertFalse(allSequences.isEmpty());
	}
	
	/**
     * Tests the expected amount of ActionSequences. 
     * <p> Fails if the analysis does not find two sequences
     */
	@Test
	public void testDataFlowAnalysisPathCount() {
		var allSequences = analysis.findAllSequences();
		assertEquals(2, allSequences.size());
	}

	/**
     * Tests the expected sequence of elements
     * <p> Fails if the analysis does not find the correct Classes for the first sequence
     */
	@Test
	public void testDataFlowAnalysisPath() {
		var allSequences = analysis.findAllSequences();
		assertEquals(2, allSequences.size());
		
		assertSequenceElements(allSequences.get(0), 
				CallingUserActionSequenceElement.class, // UserAction-Call: Store CreditCardDetails
				CallingUserActionSequenceElement.class, // UserAction-Return: Store CreditCardDetails
				
				CallingUserActionSequenceElement.class, // UserAction-Call: Look for flights
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: Request flights from Airline
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: Read flights from Database
				CallingSEFFActionSequenceElement.class, // SEFFAction-Return: Read flights from Database
				SEFFActionSequenceElement.class,		// SEFFAction: Select flights based on query and return selection
				CallingSEFFActionSequenceElement.class,	// SEFFAction-Return: Request flights from Airline
				SEFFActionSequenceElement.class,		// SEFFAction: Return found flights
				CallingUserActionSequenceElement.class, // UserAction-Return: Look for flights1
				
				CallingUserActionSequenceElement.class, // UserAction-Call: Load CreditCardDetails
				CallingUserActionSequenceElement.class, // UserAction-Return: Load CreditCardDetails
				
				CallingUserActionSequenceElement.class, // UserAction-Call: Book flight using CreditCardDetails
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: Ask Airline to book flight
				SEFFActionSequenceElement.class,		// SEFF-Action: Return confirmation
				CallingSEFFActionSequenceElement.class, // SEFFAction-Return: Ask Airline to book flight
				SEFFActionSequenceElement.class,		// SEFF-Action: Return confirmation
				CallingUserActionSequenceElement.class);// UserAction-Return: Book flight using CreditCardDetails
		
		assertSequenceElements(allSequences.get(1),	
				CallingUserActionSequenceElement.class,  //UserAction-Call: Add scheduled flight
				CallingUserActionSequenceElement.class); //UserAction-Return: Add scheduled flight
	}

	/**
     * Tests the content of the action sequences
     * <p> Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testDataFlowAnalysisContent() {
    	var allSequences = analysis.findAllSequences();
    	assertTrue(allSequences.size() > 0);
    	
    	assertUserSequenceElementContent(allSequences.get(0), 2, "look for flights");
    	assertSEFFSequenceElementContent(allSequences.get(0), 13, "ask airline to book flight");
    }
}
