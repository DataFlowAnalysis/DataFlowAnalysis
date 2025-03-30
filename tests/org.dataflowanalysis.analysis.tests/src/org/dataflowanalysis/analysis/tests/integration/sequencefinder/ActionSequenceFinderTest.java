package org.dataflowanalysis.analysis.tests.integration.sequencefinder;

import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.assertSequenceElements;
import static org.dataflowanalysis.analysis.tests.integration.AnalysisUtils.assertUserSequenceElementContent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
import org.dataflowanalysis.analysis.tests.integration.BaseTest;
import org.junit.jupiter.api.Test;

public class ActionSequenceFinderTest extends BaseTest {
    private final Logger logger = Logger.getLogger(ActionSequenceFinderTest.class);

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testTravelPlannerCount() {
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.travelPlannerPaths.size(), flowGraph.getTransposeFlowGraphs()
                .size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getTransposeFlowGraphs()
                        .size()));
        flowGraph.getTransposeFlowGraphs()
                .forEach(logger::trace);
    }

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testInternationalOnlineShopCount() {
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();
        internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.internationalOnlineShopPaths.size(), flowGraph.getTransposeFlowGraphs()
                .size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getTransposeFlowGraphs()
                        .size()));
        flowGraph.getTransposeFlowGraphs()
                .forEach(logger::trace);
    }

    /**
     * Tests whether the analysis finds the correct amount of sequences
     */
    @Test
    public void testOnlineShopCount() {
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        assertEquals(ActionSequenceFinderPaths.onlineShopPaths.size(), flowGraph.getTransposeFlowGraphs()
                .size(),
                String.format("Expected two dataflow sequences, but found %s sequences", flowGraph.getTransposeFlowGraphs()
                        .size()));
        flowGraph.getTransposeFlowGraphs()
                .forEach(logger::trace);
    }

    @Test
    public void testTravelPlannerPath() {
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();

        assertTrue(flowGraph.getTransposeFlowGraphs()
                .size() >= ActionSequenceFinderPaths.travelPlannerPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.travelPlannerPaths.size(); i++) {
            assertSequenceElements(flowGraph.getTransposeFlowGraphs()
                    .get(i), ActionSequenceFinderPaths.travelPlannerPaths.get(i));
        }
    }

    @Test
    public void testInternationalOnlineShopPath() {
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();

        assertTrue(flowGraph.getTransposeFlowGraphs()
                .size() >= ActionSequenceFinderPaths.internationalOnlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.internationalOnlineShopPaths.size(); i++) {
            assertSequenceElements(flowGraph.getTransposeFlowGraphs()
                    .get(i), ActionSequenceFinderPaths.internationalOnlineShopPaths.get(i));
        }
    }

    @Test
    public void testOnlineShopPath() {
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();

        assertTrue(flowGraph.getTransposeFlowGraphs()
                .size() >= ActionSequenceFinderPaths.onlineShopPaths.size());

        for (int i = 0; i < ActionSequenceFinderPaths.onlineShopPaths.size(); i++) {
            assertSequenceElements(flowGraph.getTransposeFlowGraphs()
                    .get(i), ActionSequenceFinderPaths.onlineShopPaths.get(i));
        }
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testTravelPlannerSEFFContent() {
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();
        assertSEFFSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 27, "ask airline to book flight");
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testInternationalOnlineShopSEFFContent() {
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();
        assertSEFFSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 17, "DatabaseStoreUserData");
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testOnlineShopSEFFContent() {
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();
        assertSEFFSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 3, "DatabaseLoadInventory");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testTravelPlannerUserContent() {
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();
        assertUserSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 5, "look for flights");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testInternationalOnlineShopUserContent() {
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();
        assertUserSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 11, "BuyEntryLevelSystemCall");
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the ActionSequence
     */
    @Test
    public void testOnlineShopUserContent() {
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();
        assertUserSequenceElementContent(flowGraph.getTransposeFlowGraphs()
                .get(0), 1, "ViewEntryLevelSystemCall");
    }
}
