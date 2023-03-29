package org.palladiosimulator.dataflow.confidentiality.analysis.propagation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicAbsent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicPresent;

import org.apache.log4j.Level;
import org.junit.jupiter.api.Test;
import org.palladiosimulator.dataflow.confidentiality.analysis.BaseTest;

public class LabelPropagationTest extends BaseTest {

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
	@Test
	public void travelPlannerCharacteristicsPresentTest() {
		travelPlannerAnalysis.setLoggerLevel(Level.TRACE);
        var sequences = travelPlannerAnalysis.findAllSequences();
        var propagationResult = travelPlannerAnalysis.evaluateDataFlows(sequences);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.travelPlannerCharacteristics) {
            assertTrue(propagationResult.size() >= characteristicData.getSequenceIndex());

            assertCharacteristicPresent(propagationResult.get(characteristicData.getSequenceIndex()),
                    characteristicData.getElementIndex(), characteristicData.getVariable(),
                    characteristicData.getCharacteristicType(), characteristicData.getCharacteristicValue());
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
        var sequences = internationalOnlineShopAnalysis.findAllSequences();
        var propagationResult = internationalOnlineShopAnalysis.evaluateDataFlows(sequences);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.internationalOnlineShopCharacteristics) {
            assertTrue(propagationResult.size() >= characteristicData.getSequenceIndex());

            assertCharacteristicPresent(propagationResult.get(characteristicData.getSequenceIndex()),
                    characteristicData.getElementIndex(), characteristicData.getVariable(),
                    characteristicData.getCharacteristicType(), characteristicData.getCharacteristicValue());
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
        var sequences = onlineShopAnalysis.findAllSequences();
        var propagationResult = onlineShopAnalysis.evaluateDataFlows(sequences);

        for (CharacteristicsData characteristicData : LabelPropagationCharacteristics.onlineShopCharacteristics) {
            assertTrue(propagationResult.size() >= characteristicData.getSequenceIndex());

            assertCharacteristicPresent(propagationResult.get(characteristicData.getSequenceIndex()),
                    characteristicData.getElementIndex(), characteristicData.getVariable(),
                    characteristicData.getCharacteristicType(), characteristicData.getCharacteristicValue());
        }
	}
	
	 /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
	@Test
	public void travelPlannerCharacteristicsAbsentTest() {
		var sequences = travelPlannerAnalysis.findAllSequences();
        var propagationResult = travelPlannerAnalysis.evaluateDataFlows(sequences);

        assertTrue(propagationResult.size() >= 2);

        assertCharacteristicAbsent(propagationResult.get(1), 2, "ccd", "AssignedRoles",
                "User");
        assertCharacteristicAbsent(propagationResult.get(1), 6, "RETURN", "GrantedRoles",
                "User");
        assertCharacteristicAbsent(propagationResult.get(1), 6, "RETURN", "GrantedRoles",
                "Airline");
	}
	
	/**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
	@Test
	public void internationalOnlineShopCharacteristicsAbsentTest() {
		var sequences = internationalOnlineShopAnalysis.findAllSequences();
        var propagationResult = internationalOnlineShopAnalysis.evaluateDataFlows(sequences);

        assertTrue(propagationResult.size() >= 1);

        assertCharacteristicAbsent(propagationResult.get(0), 0, "inventory", "DataSensitivity",
                "Public");
        assertCharacteristicAbsent(propagationResult.get(0), 1, "RETURN", "DataSensitivity",
                "Public");
	}
	
	/**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
	@Test
	public void onlineShopCharacteristicsAbsentTest() {
		var sequences = onlineShopAnalysis.findAllSequences();
        var propagationResult = onlineShopAnalysis.evaluateDataFlows(sequences);

        assertTrue(propagationResult.size() >= 1);

        assertCharacteristicAbsent(propagationResult.get(1), 0, "RETURN", "DataSensitivity",
                "Public");
	}
}
