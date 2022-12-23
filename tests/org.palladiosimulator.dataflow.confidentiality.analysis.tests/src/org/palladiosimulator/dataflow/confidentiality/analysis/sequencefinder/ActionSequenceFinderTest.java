package org.palladiosimulator.dataflow.confidentiality.analysis.sequencefinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertUserSequenceElementContent;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisFeatureTest;
import org.palladiosimulator.dataflow.confidentiality.analysis.StandalonePCMDataFlowConfidentialtyAnalysis;

public class ActionSequenceFinderTest extends AnalysisFeatureTest {

    /**
     * Tests whether the analysis finds the correct amount of sequences
     * 
     * @param analysis
     *            Analysis model that should be tested
     * @param expectedSequences
     *            Expected number of sequences
     */
    @ParameterizedTest
    @DisplayName("Should return the correct number of sequences")
    @MethodSource("testCountProvider")
    public void testCount(StandalonePCMDataFlowConfidentialtyAnalysis analysis, int expectedSequences) {
        var allSequences = analysis.findAllSequences();
        assertEquals(expectedSequences, allSequences.size(),
                String.format("Expected two dataflow sequences, but found %s sequences", allSequences.size()));
    }

    /**
     * Provides a list of arguments to the {@code testCount} Test. The list contains the arguments
     * to the test method
     * 
     * @return List of arguments to the test method
     */
    private Stream<Arguments> testCountProvider() {
        return Stream.of(Arguments.of(onlineShopAnalysis, 2), Arguments.of(internationalOnlineShopAnalysis, 1),
                Arguments.of(travelPlannerAnalysis, 2));
    }

    @ParameterizedTest
    @DisplayName("Should return the correct sequence of Dataflow Variables")
    @MethodSource("testPathProvider")
    public void testPath(StandalonePCMDataFlowConfidentialtyAnalysis analysis, List<List<Class<?>>> classes) {
        var sequences = analysis.findAllSequences();

        assertTrue(sequences.size() >= classes.size());

        for (int i = 0; i < classes.size(); i++) {
            assertSequenceElements(sequences.get(i), classes.get(i));
        }
    }

    /**
     * Provides a list of arguments to the {@code testPath} Test. The list contains the arguments to
     * the test method. The Paths for all test models can be found in {@link ActionSequenceFinderPaths}
     * 
     * @return List of arguments to the test method
     */
    private Stream<Arguments> testPathProvider() {
        return Stream.of(
                Arguments.of(onlineShopAnalysis, ActionSequenceFinderPaths.onlineShopPaths),
                Arguments.of(internationalOnlineShopAnalysis, ActionSequenceFinderPaths.internationalOnlineShopPaths),
                Arguments.of(travelPlannerAnalysis, ActionSequenceFinderPaths.travelPlannerPaths));
    }

    /**
     * Tests the content of some SEFF Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @ParameterizedTest
    @DisplayName("Action sequence should contain correct SEFF action content")
    @MethodSource("testSEFFContentProvider")
    public void testSEFFContent(StandalonePCMDataFlowConfidentialtyAnalysis analyis, Map<Integer, String> expected) {
        var sequences = analyis.findAllSequences();

        for (var entry : expected.entrySet()) {
            assertSEFFSequenceElementContent(sequences.get(0), entry.getKey(), entry.getValue());
        }
    }

    /**
     * Provides a list of arguments to the {@code testSEFFContent} Test. The list contains the
     * arguments to the test method
     * 
     * @return List of arguments to the test method
     */
    private Stream<Arguments> testSEFFContentProvider() {
        return Stream.of(Arguments.of(onlineShopAnalysis, Map.of(2, "DatabaseLoadInventory")),
                Arguments.of(internationalOnlineShopAnalysis, Map.of(13, "DatabaseStoreUserData")),
                Arguments.of(travelPlannerAnalysis, Map.of(19, "ask airline to book flight")));
    }

    /**
     * Tests the content of some User Action Sequence Elements
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @ParameterizedTest
    @DisplayName("Action sequence should contain correct user action content")
    @MethodSource("testUserContentProvider")
    public void testUserContent(StandalonePCMDataFlowConfidentialtyAnalysis analysis, Map<Integer, String> expected) {
        var sequences = analysis.findAllSequences();

        for (var entry : expected.entrySet()) {
            assertUserSequenceElementContent(sequences.get(0), entry.getKey(), entry.getValue());
        }
    }

    /**
     * Provides a list of arguments to the {@code testUserContent} Test. The list contains the
     * arguments to the test method
     * 
     * @return List of arguments to the test method
     */
    private Stream<Arguments> testUserContentProvider() {
        return Stream.of(Arguments.of(onlineShopAnalysis, Map.of(0, "ViewEntryLevelSystemCall")),
                Arguments.of(internationalOnlineShopAnalysis, Map.of(8, "BuyEntryLevelSystemCall")),
                Arguments.of(travelPlannerAnalysis, Map.of(3, "look for flights")));
    }
}