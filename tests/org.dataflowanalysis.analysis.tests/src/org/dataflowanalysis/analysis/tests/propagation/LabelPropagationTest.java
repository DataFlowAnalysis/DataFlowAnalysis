package org.dataflowanalysis.analysis.tests.propagation;

import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertCharacteristicAbsent;
import static org.dataflowanalysis.analysis.tests.AnalysisUtils.assertCharacteristicPresent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraphCollection;
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
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.travelPlannerCharacteristics) {
            assertTrue(flowGraph.getTransposeFlowGraphs()
                    .size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(flowGraph.getTransposeFlowGraphs()
                    .get(characteristicData.sequenceIndex()), characteristicData.elementIndex(), characteristicData.variable(),
                    characteristicData.characteristicType(), characteristicData.characteristicValue());
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
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.internationalOnlineShopCharacteristics) {
            assertTrue(flowGraph.getTransposeFlowGraphs()
                    .size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(flowGraph.getTransposeFlowGraphs()
                    .get(characteristicData.sequenceIndex()), characteristicData.elementIndex(), characteristicData.variable(),
                    characteristicData.characteristicType(), characteristicData.characteristicValue());
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
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.onlineShopCharacteristics) {
            assertTrue(flowGraph.getTransposeFlowGraphs()
                    .size() >= characteristicData.sequenceIndex());

            assertCharacteristicPresent(flowGraph.getTransposeFlowGraphs()
                    .get(characteristicData.sequenceIndex()), characteristicData.elementIndex(), characteristicData.variable(),
                    characteristicData.characteristicType(), characteristicData.characteristicValue());
        }
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void travelPlannerCharacteristicsAbsentTest() {
        PCMFlowGraphCollection flowGraph = travelPlannerAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        assertTrue(flowGraph.getTransposeFlowGraphs()
                .size() >= 2);

        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(0), 2, "ccd", "AssignedRoles", "User");
        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(0), 6, "RETURN", "GrantedRoles", "User");
        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(0), 6, "RETURN", "GrantedRoles", "Airline");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void internationalOnlineShopCharacteristicsAbsentTest() {
        PCMFlowGraphCollection flowGraph = internationalOnlineShopAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        assertFalse(flowGraph.getTransposeFlowGraphs()
                .isEmpty());

        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(0), 0, "inventory", "DataSensitivity", "Public");
        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(0), 1, "RETURN", "DataSensitivity", "Public");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void onlineShopCharacteristicsAbsentTest() {
        PCMFlowGraphCollection flowGraph = onlineShopAnalysis.findFlowGraphs();
        flowGraph.evaluate();

        assertFalse(flowGraph.getTransposeFlowGraphs()
                .isEmpty());

        assertCharacteristicAbsent(flowGraph.getTransposeFlowGraphs()
                .get(1), 0, "RETURN", "DataSensitivity", "Public");
    }
}
