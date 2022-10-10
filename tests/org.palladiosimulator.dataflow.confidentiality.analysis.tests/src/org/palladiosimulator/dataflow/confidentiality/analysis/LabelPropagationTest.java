package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.Assert.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicAbsent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicPresent;

import org.junit.jupiter.api.Test;

public class LabelPropagationTest extends AnalysisFeatureTest {

    /**
     * Tests the present or absent characteristics of the found action sequences of the travel
     * planner example
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void travelPlannerLabelTest() {
        var allSequences = travelPlannerAnalysis.findAllSequences();
        var sequences = travelPlannerAnalysis.evaluateDataFlows(allSequences);

        assertTrue(sequences.size() > 0);

        assertCharacteristicPresent(sequences.get(0), 0, "ccd", "GrantedRoles", "User");
        assertCharacteristicPresent(sequences.get(0), 2, "query", "GrantedRoles", "User");
        assertCharacteristicPresent(sequences.get(0), 2, "query", "GrantedRoles", "Airline");
        assertCharacteristicAbsent(sequences.get(0), 6, "RETURN", "GrantedRoles", "User");
        assertCharacteristicAbsent(sequences.get(0), 6, "RETURN", "GrantedRoles", "Airline");

        assertCharacteristicPresent(sequences.get(1), 0, "flight", "GrantedRoles", "User");
        assertCharacteristicPresent(sequences.get(1), 0, "flight", "GrantedRoles", "Airline");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences of the online shop
     * example
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void onlineShopLabelTest() {
        var allSequences = onlineShopAnalysis.findAllSequences();
        var sequences = onlineShopAnalysis.evaluateDataFlows(allSequences);

        assertTrue(sequences.size() > 0);

        assertCharacteristicAbsent(sequences.get(0), 0, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 2, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 4, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 5, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 7, "userData", "DataSensitivity", "Personal");

        assertCharacteristicPresent(sequences.get(1), 8, "userData", "DataSensitivity", "Personal");
    }

    /**
     * Tests the present or absent characteristics of the found action sequences of the
     * international online shop example
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @Test
    public void internationalOnlineShopLabelTest() {
        var allSequences = internationalOnlineShopAnalysis.findAllSequences();
        var sequences = internationalOnlineShopAnalysis.evaluateDataFlows(allSequences);

        assertTrue(sequences.size() > 0);

        sequences.forEach(System.out::println);

        assertCharacteristicAbsent(sequences.get(0), 0, "inventory", "DataSensitivity", "Public");
        assertCharacteristicAbsent(sequences.get(0), 1, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 2, "RETURN", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 5, "inventory", "DataSensitivity", "Public");
        assertCharacteristicPresent(sequences.get(0), 6, "userData", "DataSensitivity", "Personal");
        assertCharacteristicPresent(sequences.get(0), 9, "userData", "DataSensitivity", "Personal");
    }
}
