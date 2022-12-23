package org.palladiosimulator.dataflow.confidentiality.analysis.propagation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.TEST_MODEL_PROJECT_NAME;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicAbsent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertCharacteristicPresent;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisFeatureTest;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.testmodels.Activator;

public class LabelPropagationTest extends AnalysisFeatureTest {

    /**
     * Tests the present characteristics of the found action sequences
     * <p>
     * Fails if the analysis does not propagate the correct characteristics to each ActionSequence
     */
    @ParameterizedTest
    @DisplayName("Sequence elements should have correct characteristics present")
    @MethodSource("characteristicsPresentProvider")
    public void characteristicsPresentTest(StandalonePCMDataFlowConfidentialtyAnalysis analysis, List<CharacteristicsData> characteristicsData) {
    	analysis.setLoggerLevel(Level.TRACE);
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        for (CharacteristicsData characteristicData : characteristicsData) {
            assertTrue(propagationResult.size() >= characteristicData.getSequenceIndex());

            assertCharacteristicPresent(propagationResult.get(characteristicData.getSequenceIndex()),
                    characteristicData.getElementIndex(), characteristicData.getVariable(),
                    characteristicData.getCharacteristicType(), characteristicData.getCharacteristicValue());
        }
    }

    /**
     * Provides test data to the {@code characteristicsPresentTest} Test class.
     * <p>
     * Each invocation contains the index into the list of sequences and a map which maps a index
     * into the action sequence to the expected characteristic
     * 
     * @return Returns a stream of test data used for each invocation
     */
    private Stream<Arguments> characteristicsPresentProvider() {
        return Stream.of(
                Arguments.of(travelPlannerAnalysis, LabelPropagationCharacteristics.travelPlannerCharacteristics),
                Arguments.of(onlineShopAnalysis, LabelPropagationCharacteristics.onlineShopCharacteristics),
                Arguments.of(internationalOnlineShopAnalysis,
                        LabelPropagationCharacteristics.internationalOnlineShopCharacteristics));
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
    private Stream<Arguments> characteristicsAbsentProvider() {
        return Stream.of(
        		Arguments.of(travelPlannerAnalysis, 1, 2, "ccd", "AssignedRoles", "User"),
        		Arguments.of(travelPlannerAnalysis, 1, 6, "RETURN", "GrantedRoles", "User"),
                Arguments.of(travelPlannerAnalysis, 1, 6, "RETURN", "GrantedRoles", "Airline"),
                Arguments.of(onlineShopAnalysis, 0, 0, "RETURN", "DataSensitivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 0, "inventory", "DataSensivity", "Public"),
                Arguments.of(internationalOnlineShopAnalysis, 0, 1, "RETURN", "DataSensivity", "Public"));
    }

    @Test
    public void containerAssemblyTest() {
        final var usageModelPath = Paths.get("models", "ContainerTest", "default.usagemodel")
            .toString();
        final var allocationPath = Paths.get("models", "ContainerTest", "default.allocation")
            .toString();

        var analysis = new StandalonePCMDataFlowConfidentialtyAnalysis(TEST_MODEL_PROJECT_NAME, Activator.class,
                usageModelPath, allocationPath);

        analysis.initalizeAnalysis();

        var sequences = analysis.findAllSequences();
        var analysedSequences = analysis.evaluateDataFlows(sequences);

        var sequence = analysedSequences.get(0);
        var element = sequence.elements()
            .get(3);
        var nodeCharacteristics = element.getAllNodeCharacteristics();
        System.err.println(nodeCharacteristics.toArray());
    }
}
