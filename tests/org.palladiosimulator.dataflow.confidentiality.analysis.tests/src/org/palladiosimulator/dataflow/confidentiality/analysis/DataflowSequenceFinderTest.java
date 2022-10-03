package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSEFFSequenceElementContent;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElement;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertSequenceElements;
import static org.palladiosimulator.dataflow.confidentiality.analysis.AnalysisUtils.assertUserSequenceElementContent;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.SEFFActionSequenceElement;

public class DataflowSequenceFinderTest extends AnalysisFeatureTest {

    /**
     * Tests whether the analysis finds the correct amount of sequences
     * 
     * @param analysis
     *            Analysis model that should be tested
     * @param expectedSequences
     *            Expected number of sequences
     */
    @ParameterizedTest
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
    private static Stream<Arguments> testCountProvider() {
        return Stream.of(Arguments.of(onlineShopAnalysis, 2), Arguments.of(internationalOnlineShopAnalysis, 1),
                Arguments.of(travelPlannerAnalysis, 2));
    }

    /**
     * Tests the expected sequence of elements
     * <p>
     * Fails if the analysis does not find the correct Classes for the first sequence
     */
    @Test
    public void testOnlineShopPath() {
        var allSequences = onlineShopAnalysis.findAllSequences();

        assertEquals(allSequences.size(), 2);

        assertSequenceElements(allSequences.get(0), CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, CallingSEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class);

        assertSequenceElement(allSequences.get(1), 8, SEFFActionSequenceElement.class);
    }

    /**
     * Tests the expected sequence of elements
     * <p>
     * Fails if the analysis does not find the correct Classes for the first sequence
     */
    @Test
    public void testInternationalOnlineShopPath() {
        var allSequences = internationalOnlineShopAnalysis.findAllSequences();
        assertEquals(1, allSequences.size());

        assertSequenceElements(allSequences.get(0), CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, CallingSEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, CallingSEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class);
    }

    /**
     * Tests the expected sequence of elements
     * <p>
     * Fails if the analysis does not find the correct Classes for the first sequence
     */
    @Test
    public void testTravelPlannerPath() {
        var allSequences = travelPlannerAnalysis.findAllSequences();
        assertEquals(2, allSequences.size());

        assertSequenceElements(allSequences.get(0), CallingUserActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, CallingSEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingUserActionSequenceElement.class, CallingUserActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingSEFFActionSequenceElement.class, SEFFActionSequenceElement.class,
                CallingUserActionSequenceElement.class);

        assertSequenceElements(allSequences.get(1), CallingUserActionSequenceElement.class,
                CallingUserActionSequenceElement.class);
    }

    /**
     * Tests the content of the action sequences
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testOnlineShopContent() {
        var allSequences = onlineShopAnalysis.findAllSequences();
        assertTrue(allSequences.size() > 0);

        assertUserSequenceElementContent(allSequences.get(0), 0, "ViewEntryLevelSystemCall");
        assertSEFFSequenceElementContent(allSequences.get(0), 1, "DatabaseLoadInventory");
    }

    /**
     * Tests the content of the action sequences
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testInternationalOnlineShopContent() {
        var allSequences = internationalOnlineShopAnalysis.findAllSequences();
        assertTrue(allSequences.size() > 0);

        assertUserSequenceElementContent(allSequences.get(0), 6, "BuyEntryLevelSystemCall");
        assertSEFFSequenceElementContent(allSequences.get(0), 9, "DatabaseStoreUserData");
    }

    /**
     * Tests the content of the action sequences
     * <p>
     * Fails if the analysis does not find the correct entity name for elements in the
     * ActionSequence
     */
    @Test
    public void testTravelPlannerContent() {
        var allSequences = travelPlannerAnalysis.findAllSequences();
        assertTrue(allSequences.size() > 0);

        assertUserSequenceElementContent(allSequences.get(0), 2, "look for flights");
        assertSEFFSequenceElementContent(allSequences.get(0), 13, "ask airline to book flight");
    }
}
