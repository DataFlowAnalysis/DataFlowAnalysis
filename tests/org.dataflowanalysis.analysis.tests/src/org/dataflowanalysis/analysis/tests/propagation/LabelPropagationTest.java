package org.dataflowanalysis.analysis.tests.propagation;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertCharacteristicAbsent;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertCharacteristicPresent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.tests.BaseTest;
import org.junit.jupiter.api.Test;

public class LabelPropagationTest extends BaseTest {

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void travelPlannerCharacteristicsPresentTest() {
        travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = travelPlannerAnalysis.evaluateFlowGraph(flowGraph);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.travelPlannerCharacteristics) {
            assertTrue(propagatedFlowGraph.getPartialFlowGraphs().size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(propagatedFlowGraph.getPartialFlowGraphs().get(characteristicData.sequenceIndex()),
                    characteristicData.elementIndex(), characteristicData.variable(), characteristicData.characteristicType(),
                    characteristicData.characteristicValue());
        }
    }

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void internationalOnlineShopCharacteristicsPresentTest() {
        internationalOnlineShopAnalysis.setLoggerLevel(Level.TRACE);
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = internationalOnlineShopAnalysis.evaluateFlowGraph(flowGraph);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.internationalOnlineShopCharacteristics) {
            assertTrue(propagatedFlowGraph.getPartialFlowGraphs().size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(propagatedFlowGraph.getPartialFlowGraphs().get(characteristicData.sequenceIndex()),
                    characteristicData.elementIndex(), characteristicData.variable(), characteristicData.characteristicType(),
                    characteristicData.characteristicValue());
        }
    }

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void onlineShopCharacteristicsPresentTest() {
        onlineShopAnalysis.setLoggerLevel(Level.TRACE);
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = onlineShopAnalysis.evaluateFlowGraph(flowGraph);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.onlineShopCharacteristics) {
            assertTrue(propagatedFlowGraph.getPartialFlowGraphs().size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(propagatedFlowGraph.getPartialFlowGraphs().get(characteristicData.sequenceIndex()),
                    characteristicData.elementIndex(), characteristicData.variable(), characteristicData.characteristicType(),
                    characteristicData.characteristicValue());
        }
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void travelPlannerCharacteristicsAbsentTest() {
        PCMFlowGraph flowGraph = travelPlannerAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = travelPlannerAnalysis.evaluateFlowGraph(flowGraph);

        assertTrue(propagatedFlowGraph.getPartialFlowGraphs().size() >= 2);

        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(0), 2, "ccd", "AssignedRoles", "User");
        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(0), 6, "RETURN", "GrantedRoles", "User");
        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(0), 6, "RETURN", "GrantedRoles", "Airline");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void internationalOnlineShopCharacteristicsAbsentTest() {
        PCMFlowGraph flowGraph = internationalOnlineShopAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = internationalOnlineShopAnalysis.evaluateFlowGraph(flowGraph);

        assertFalse(propagatedFlowGraph.getPartialFlowGraphs().isEmpty());

        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(0), 0, "inventory", "DataSensitivity", "Public");
        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(0), 1, "RETURN", "DataSensitivity", "Public");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void onlineShopCharacteristicsAbsentTest() {
        PCMFlowGraph flowGraph = onlineShopAnalysis.findFlowGraph();
        PCMFlowGraph propagatedFlowGraph = onlineShopAnalysis.evaluateFlowGraph(flowGraph);

        assertFalse(propagatedFlowGraph.getPartialFlowGraphs().isEmpty());

        assertCharacteristicAbsent(propagatedFlowGraph.getPartialFlowGraphs().get(1), 0, "RETURN", "DataSensitivity", "Public");
    }
}
