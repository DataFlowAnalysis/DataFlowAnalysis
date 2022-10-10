package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.Assert.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicAbsent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicPresent;

import org.junit.jupiter.api.Test;

public class LabelPropagationTest extends AnalysisFeatureTest {

    /**
     * Tests the present or absent characteristics of the found action sequences
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
}
