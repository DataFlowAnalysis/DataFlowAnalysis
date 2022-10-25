package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicAbsent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicPresent;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LabelPropagationTest extends AnalysisFeatureTest {

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @ParameterizedTest(name = "{index}. {3}.{4}.{5} present at index {2} in action sequence")
    @DisplayName("Sequence elements should have correct characteristics present")
    @MethodSource("characteristicsPresentProvider")
    public void characteristicsPresentTest(StandalonePCMDataFlowConfidentialtyAnalysis analysis, int sequenceIndex,
            int elementIndex, String variable, String characteristicType, String characteristicValue) {
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        assertTrue(propagationResult.size() >= sequenceIndex);

        assertCharacteristicPresent(propagationResult.get(sequenceIndex), elementIndex, variable, characteristicType,
                characteristicValue);
    }

    /**
     * Provides test data to the {@code characteristicsPresentTest} Test class.
     * <p>
     * Each invocation contains the index into the list of sequences and a map which maps a index
     * into the action sequence to the expected characteristic
     * 
     * @return Returns a stream of test data used for each invocation
     */
    private static Stream<Arguments> characteristicsPresentProvider() {
        return Stream.of(Arguments.of(travelPlannerAnalysis, 0, 0, "ccd", "GrantedRoles", "User"),
                Arguments.of(travelPlannerAnalysis, 0, 2, "query", "GrantedRoles", "User"),
                Arguments.of(travelPlannerAnalysis, 0, 2, "query", "GrantedRoles", "Airline"),
                Arguments.of(travelPlannerAnalysis, 1, 0, "flight", "GrantedRoles", "User"),
                Arguments.of(travelPlannerAnalysis, 1, 0, "flight", "GrantedRoles", "Airline"),
                Arguments.of(onlineShopAnalysis, 0, 2, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(onlineShopAnalysis, 0, 4, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(onlineShopAnalysis, 0, 5, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(onlineShopAnalysis, 0, 7, "userData", "DataSensitivity", "Personal"),
                Arguments.of(onlineShopAnalysis, 1, 8, "userData", "DataSensitivity", "Personal"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 2, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 5, "inventory", "DataSensitivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 6, "userData", "DataSensitivity", "Personal"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 9, "userData", "DataSensitivity", "Personal"));
    }

    /**
     * Tests the present or absent characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @ParameterizedTest(name = "{index}. {3}.{4}.{5} absent at index {2} in action sequence")
    @DisplayName("Sequence elements should have correct characteristics absent")
    @MethodSource("characteristicsAbsentProvider")
    public void characteristicsAbsentTest(StandalonePCMDataFlowConfidentialtyAnalysis analysis, int sequenceIndex,
            int elementIndex, String variable, String characteristicType, String characteristicValue) {
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        assertTrue(propagationResult.size() >= sequenceIndex);

        assertCharacteristicAbsent(propagationResult.get(sequenceIndex), elementIndex, variable, characteristicType,
                characteristicValue);
    }

    /**
     * Provides test data to the {@code characteristicsPresentTest} Test class.
     * <p>
     * Each invocation contains the index into the list of sequences and a map which maps a index
     * into the action sequence to the expected characteristic
     * 
     * @return Returns a stream of test data used for each invocation
     */
    private static Stream<Arguments> characteristicsAbsentProvider() {
        return Stream.of(Arguments.of(travelPlannerAnalysis, 0, 6, "RETURN", "GrantedRoles", "User"),
                Arguments.of(travelPlannerAnalysis, 0, 6, "RETURN", "GrantedRoles", "Airline"),
                Arguments.of(onlineShopAnalysis, 0, 0, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 0, "inventory", "DataSensivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 1, "RETURN", "DataSensivity", "Public"));
    }
}
