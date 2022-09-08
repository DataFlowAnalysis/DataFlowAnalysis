package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElement;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertUserSequenceElementContent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSEFFSequenceElementContent;

import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

public class DataFlowConfidentialityAnalysisTest {
	
	private static StandalonePCMDataFlowConfidentialtyAnalysis analysis;
	
	@BeforeAll
	public static void initializeAnalysis() {
        final var usageModelPath = Paths.get("models", "BranchingOnlineShop", "default.usagemodel").toString();
        final var allocationPath = Paths.get("models", "BranchingOnlineShop", "default.allocation").toString();
        
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
    public void testPCMActionSequenceFinderCount() {
    	var allSequences = analysis.findAllSequences();
    	assertEquals(2, allSequences.size(), String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
    }

    @Test
    public void testPCMActionSequenceFinderTypes() {
        var allSequences = analysis.findAllSequences();

        assertEquals(allSequences.size(), 2);

        assertSequenceElements(allSequences.get(0), 
        		CallingUserActionSequenceElement.class, // UserAction-Call: ViewEntryLevelSystemCall
                CallingSEFFActionSequenceElement.class, // SEFFAction-Call: DatabaseLoadInventory
                SEFFActionSequenceElement.class,		// SEFFAction: Return
                CallingSEFFActionSequenceElement.class, // SEFFAction-Return: DatabaseLoadInventory
                SEFFActionSequenceElement.class,		// SEFFAction: Return
                CallingUserActionSequenceElement.class, // UserAction-Return: ViewEntryLevelSystemCall
                
                CallingUserActionSequenceElement.class, // UserAction-Call: BuyEntryLevelSystemCall
                CallingSEFFActionSequenceElement.class, // SEFFAction-Call: SaveInventoryCall
                CallingSEFFActionSequenceElement.class, // SEFFAction-Return: SaveInventoryCall
                CallingUserActionSequenceElement.class);// UserAction-Return: BuyEntryLevelSystemCall

        assertSequenceElement(allSequences.get(1), 8, SEFFActionSequenceElement.class);
    }

    @Test
    public void testPCMActionSequenceFinderContent() {
    	var allSequences = analysis.findAllSequences();
    	assertTrue(allSequences.size() > 0);
    	
    	assertUserSequenceElementContent(allSequences.get(0), 0, "ViewEntryLevelSystemCall");
    	assertSEFFSequenceElementContent(allSequences.get(0), 1, "DatabaseLoadInventory");
    }
}
