package org.palladiosimulator.dataflow.confidentiality.analysis.sequencefinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertUserSequenceElementContent;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;

import org.palladiosimulator.dataflow.confidentiality.analysis.BaseTest;

public class ActionSequenceFinderTest extends BaseTest {
	private final Logger logger = Logger.getLogger(ActionSequenceFinderTest.class);

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */   
    @Test
    public void testTravelPlannerCount() {
    	var allSequences = travelPlannerAnalysis.findAllSequences();
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.travelPlannerPaths.size(), allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
        allSequences.stream().forEach(logger::trace);
    }
    
    /**
     * Tests whether the analysis finds the correct amount of sequences
     */   
    @Test
    public void testInternationalOnlineShopCount() {
    	var allSequences = internationalOnlineShopAnalysis.findAllSequences();
        internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.internationalOnlineShopPaths.size(), allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
        allSequences.stream().forEach(logger::trace);
    }
    
    /**
     * Tests whether the analysis finds the correct amount of sequences
     */   
    @Test
    public void testOnlineShopCount() {
    	var allSequences = onlineShopAnalysis.findAllSequences();
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.onlineShopPaths.size(), allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
        allSequences.stream().forEach(logger::trace);
    }
    
    @Test
    public void testTravelPlannerPath() {
    	var sequences = travelPlannerAnalysis.findAllSequences();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.travelPlannerPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.travelPlannerPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.travelPlannerPaths.get(i));
        }
    }
    
    @Test
    public void testInternationalOnlineShopPath() {
    	var sequences = internationalOnlineShopAnalysis.findAllSequences();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.internationalOnlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.internationalOnlineShopPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.internationalOnlineShopPaths.get(i));
        }
    }
    
    @Test
    public void testOnlineShopPath() {
    	var sequences = onlineShopAnalysis.findAllSequences();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.onlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.onlineShopPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.onlineShopPaths.get(i));
        }
    }
    
    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testTravelPlannerSEFFContent() {
    	var sequences = travelPlannerAnalysis.findAllSequences();
        assertSEFFSequenceElementContent(sequences.get(0), 19, "ask airline to book flight");
    }
    
    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testInternationalOnlineShopSEFFContent() {
    	var sequences = internationalOnlineShopAnalysis.findAllSequences();
        assertSEFFSequenceElementContent(sequences.get(0), 13, "DatabaseStoreUserData");
    }
    
    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testOnlineShopSEFFContent() {
    	var sequences = onlineShopAnalysis.findAllSequences();
        assertSEFFSequenceElementContent(sequences.get(0), 2, "DatabaseLoadInventory");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testTravelPlannerUserContent() {
    	var sequences = travelPlannerAnalysis.findAllSequences();
        assertUserSequenceElementContent(sequences.get(0), 3, "look for flights");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testInternationalOnlineShopUserContent() {
    	var sequences = internationalOnlineShopAnalysis.findAllSequences();
        assertUserSequenceElementContent(sequences.get(0), 8, "BuyEntryLevelSystemCall");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testOnlineShopUserContent() {
    	var sequences = onlineShopAnalysis.findAllSequences();
        assertUserSequenceElementContent(sequences.get(0), 0, "ViewEntryLevelSystemCall");
    }
}
