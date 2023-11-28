package org.dataflowanalysis.analysis.tests.sequencefinder;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSequenceElements;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertUserSequenceElementContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Until model update")
public class ActionSequenceFinderTest extends BaseTest {
	private final Logger logger = Logger.getLogger(ActionSequenceFinderTest.class);

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */   
    @Test
    @Disabled
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
    @Disabled
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
    @Disabled
    public void testOnlineShopCount() {
    	var allSequences = onlineShopAnalysis.findAllSequences();
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.onlineShopPaths.size(), allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
        allSequences.stream().forEach(logger::trace);
    }
    
    @Test
    @Disabled
    public void testTravelPlannerPath() {
    	var sequences = travelPlannerAnalysis.findAllSequences();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.travelPlannerPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.travelPlannerPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.travelPlannerPaths.get(i));
        }
    }
    
    @Test
    @Disabled
    public void testInternationalOnlineShopPath() {
    	var sequences = internationalOnlineShopAnalysis.findAllSequences();

        assertTrue(sequences.size() >= ActionSequenceFinderPaths.internationalOnlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.internationalOnlineShopPaths.size(); i++) {
            assertSequenceElements(sequences.get(i), ActionSequenceFinderPaths.internationalOnlineShopPaths.get(i));
        }
    }
    
    @Test
    @Disabled
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
    @Disabled
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
    @Disabled
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
    @Disabled
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
    @Disabled
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
    @Disabled
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
    @Disabled
    public void testOnlineShopUserContent() {
    	var sequences = onlineShopAnalysis.findAllSequences();
        assertUserSequenceElementContent(sequences.get(0), 0, "ViewEntryLevelSystemCall");
    }
}
