package org.dataflowanalysis.analysis.tests.sequencefinder;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertSequenceElements;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertUserSequenceElementContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.junit.jupiter.api.Test;

public class ActionSequenceFinderTest extends BaseTest {
    private final Logger logger = Logger.getLogger(ActionSequenceFinderTest.class);

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testTravelPlannerCount() {
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.travelPlannerPaths.size(), flowGraph.getPartialFlowGraphs().size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getPartialFlowGraphs().size()));
        flowGraph.getPartialFlowGraphs().forEach(logger::trace);
    }

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testInternationalOnlineShopCount() {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.internationalOnlineShopPaths.size(), flowGraph.getPartialFlowGraphs().size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getPartialFlowGraphs().size()));
        flowGraph.getPartialFlowGraphs().forEach(logger::trace);
    }

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testOnlineShopCount() {
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.onlineShopPaths.size(), flowGraph.getPartialFlowGraphs().size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getPartialFlowGraphs().size()));
        flowGraph.getPartialFlowGraphs().forEach(logger::trace);
    }

    @Test
    public void testTravelPlannerPath() {
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();

        assertTrue(flowGraph.getPartialFlowGraphs().size() >= ActionSequenceFinderPaths.travelPlannerPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.travelPlannerPaths.size(); i++) {
            assertSequenceElements(flowGraph.getPartialFlowGraphs().get(i), ActionSequenceFinderPaths.travelPlannerPaths.get(i));
        }
    }

    @Test
    public void testInternationalOnlineShopPath() {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();

        assertTrue(flowGraph.getPartialFlowGraphs().size() >= ActionSequenceFinderPaths.internationalOnlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.internationalOnlineShopPaths.size(); i++) {
            assertSequenceElements(flowGraph.getPartialFlowGraphs().get(i), ActionSequenceFinderPaths.internationalOnlineShopPaths.get(i));
        }
    }

    @Test
    public void testOnlineShopPath() {
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();

        assertTrue(flowGraph.getPartialFlowGraphs().size() >= ActionSequenceFinderPaths.onlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.onlineShopPaths.size(); i++) {
            assertSequenceElements(flowGraph.getPartialFlowGraphs().get(i), ActionSequenceFinderPaths.onlineShopPaths.get(i));
        }
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testTravelPlannerSEFFContent() {
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();
        assertSEFFSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 27, "ask airline to book flight");
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testInternationalOnlineShopSEFFContent() {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        assertSEFFSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 17, "DatabaseStoreUserData");
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testOnlineShopSEFFContent() {
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();
        assertSEFFSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 3, "DatabaseLoadInventory");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testTravelPlannerUserContent() {
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();
        assertUserSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 5, "look for flights");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testInternationalOnlineShopUserContent() {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        assertUserSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 11, "BuyEntryLevelSystemCall");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testOnlineShopUserContent() {
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();
        assertUserSequenceElementContent(flowGraph.getPartialFlowGraphs().get(0), 1, "ViewEntryLevelSystemCall");
    }
}
