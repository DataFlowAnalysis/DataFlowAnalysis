package org.dataflowanalysis.analysis.tests.sequencefinder;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSequenceElements;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertUserSequenceElementContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.junit.jupiter.api.Test;

public class ActionSequenceFinderTest extends BaseTest {
	private final Logger logger = Logger.getLogger(ActionSequenceFinderTest.class);

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */   
    @Test
    public void testTravelPlannerCount() {
    	var allSequences = travelPlannerAnalysis.findAllPartialFlowGraphs();
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
    	var allSequences = internationalOnlineShopAnalysis.findAllPartialFlowGraphs();
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
    	var allSequences = onlineShopAnalysis.findAllPartialFlowGraphs();
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.onlineShopPaths.size(), allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
        allSequences.stream().forEach(logger::trace);
    }
    
    @Test
    public void testTravelPlannerPath() {
    	var sequences = travelPlannerAnalysis.findAllPartialFlowGraphs();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.travelPlannerPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.travelPlannerPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.travelPlannerPaths.get(i));
        }
    }
    
    @Test
    public void testInternationalOnlineShopPath() {
    	var sequences = internationalOnlineShopAnalysis.findAllPartialFlowGraphs();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.internationalOnlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.internationalOnlineShopPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.internationalOnlineShopPaths.get(i));
        }
    }
    
    @Test
    public void testOnlineShopPath() {
    	var sequences = onlineShopAnalysis.findAllPartialFlowGraphs();

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
    	var sequences = travelPlannerAnalysis.findAllPartialFlowGraphs();
        assertSEFFSequenceElementContent(sequences.get(0), 27, "ask airline to book flight");
    }
    
    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testInternationalOnlineShopSEFFContent() {
    	var sequences = internationalOnlineShopAnalysis.findAllPartialFlowGraphs();
        assertSEFFSequenceElementContent(sequences.get(0), 17, "DatabaseStoreUserData");
    }
    
    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testOnlineShopSEFFContent() {
    	var sequences = onlineShopAnalysis.findAllPartialFlowGraphs();
        assertSEFFSequenceElementContent(sequences.get(0), 3, "DatabaseLoadInventory");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testTravelPlannerUserContent() {
    	var sequences = travelPlannerAnalysis.findAllPartialFlowGraphs();
        assertUserSequenceElementContent(sequences.get(0), 5, "look for flights");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testInternationalOnlineShopUserContent() {
    	var sequences = internationalOnlineShopAnalysis.findAllPartialFlowGraphs();
        assertUserSequenceElementContent(sequences.get(0), 11, "BuyEntryLevelSystemCall");
    }
    
    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testOnlineShopUserContent() {
    	var sequences = onlineShopAnalysis.findAllPartialFlowGraphs();
        assertUserSequenceElementContent(sequences.get(0), 1, "ViewEntryLevelSystemCall");
    }
}
