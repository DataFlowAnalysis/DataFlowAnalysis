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

public class InternationalOnlineShopDataFlowConfidentialiyAnalysisTest {
	
	private static StandalonePCMDataFlowConfidentialtyAnalysis analysis;
	
	@BeforeAll
	public static void setupAnalysis() {
        final var usageModelPath = Paths.get("models", "InternationalOnlineShop", "default.usagemodel").toString();
		final var allocationPath = Paths.get("models", "InternationalOnlineShop", "default.allocation").toString();

        analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(
                TEST_MODEL_PROJECT_NAME, Activator.class, usageModelPath, allocationPath);

        analysis.initalizeAnalysis();
	}
	
	@Test
	public void testStandaloneAnalysis() {
        var allSequences = analysis.findAllSequences();
        allSequences.stream()
            .map(it -> it.toString())
            .forEach(System.out::println);

        assertFalse(allSequences.isEmpty());
	}
	
	@Test
	public void testDataFlowAnalysisPathCount() {
		var allSequences = analysis.findAllSequences();
		assertEquals(1, allSequences.size());
	}

	@Test
	public void testDataFlowAnalysisPath() {
		var allSequences = analysis.findAllSequences();
		assertEquals(1, allSequences.size());
		
		assertSequenceElements(allSequences.get(0), 
				CallingUserActionSequenceElement.class, // UserAction-Call: ViewEntryLevelSystemCall
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: DatabaseLoadInventory
				SEFFActionSequenceElement.class,		// SEFF-Action: Return
				CallingSEFFActionSequenceElement.class,	// SEFFAction-Return: DatabaseLoadInventory
				SEFFActionSequenceElement.class,		// SEFF-Action: Return
				CallingUserActionSequenceElement.class, // UserAction-Return: ViewEntryLevelSystemCall
				
				CallingUserActionSequenceElement.class, // UserAction-Call: BuyEntryLevelSystemCall
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: DatabaseStoreInventory
				CallingSEFFActionSequenceElement.class, // SEFFAction-Return: DatabaseStoreInventory
				CallingSEFFActionSequenceElement.class, // SEFFAction-Call: DatabaseStoreUserData
				CallingSEFFActionSequenceElement.class, // SEFFAction-Return: DatabaseStoreUserData
				CallingUserActionSequenceElement.class);// UserAction-Return: BuyEntryLevelSystemCall
	}

    @Test
    public void testDataFlowAnalysisContent() {
    	var allSequences = analysis.findAllSequences();
    	assertTrue(allSequences.size() > 0);
    	
    	assertUserSequenceElementContent(allSequences.get(0), 6, "BuyEntryLevelSystemCall");
    	assertSEFFSequenceElementContent(allSequences.get(0), 9, "DatabaseStoreUserData");
    }
}
