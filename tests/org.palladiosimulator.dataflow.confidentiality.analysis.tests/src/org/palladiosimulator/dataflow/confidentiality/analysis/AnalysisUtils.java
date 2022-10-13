package org.palladiosimulator.dataflow.confidentiality.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingSEFFActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.CallingUserActionSequenceElement;

public class AnalysisUtils {

    private AnalysisUtils() {
        // Utility class
    }

    public static String TEST_MODEL_PROJECT_NAME = "org.palladiosimulator.dataflow.confidentiality.analysis.testmodels";

    /**
     * <em>Assert</em> that {@code sequence} at index {@code index} and {@code expectedType} are of
     * the same class.
     * <p>
     * If {@code sequence} are {@code null}, they are considered unequal
     * 
     * @param sequence
     *            ActionSequence to be inspected
     * @param index
     *            Index into the {@code sequence} to be compared
     * @param expectedType
     *            Expected type of the given ActionSequence at the given index
     */
    public static void assertSequenceElement(ActionSequence sequence, int index, Class<?> expectedType) {
        assertNotNull(sequence.elements());
        assertTrue(sequence.elements()
            .size() >= index + 1);

        Class<?> actualType = sequence.elements()
            .get(index)
            .getClass();

        assertEquals(expectedType, actualType, createProblemMessage(index, expectedType, actualType));
    }

    /**
     * <em>Assert</em> that the elements in {@code sequence} and {@code expectedType} are ordered
     * like the provided list of classes
     * <p>
     * If {@code sequence} is {@code null} or the sequences are of different length, they are
     * considered unequal
     * 
     * @param sequence
     *            ActionSequence to be inspected
     * @param expectedType
     *            Expected types of the given ActionSequence at all indexes
     */
    public static void assertSequenceElements(ActionSequence sequence, List<Class<?>> expectedElementTypes) {
        var elements = sequence.elements();

        assertNotNull(elements);
        assertEquals(sequence.elements()
            .size(), expectedElementTypes.size());

        for (int i = 0; i < expectedElementTypes.size(); i++) {
            Class<?> expectedType = expectedElementTypes.get(i);
            Class<?> actualType = elements.get(i)
                .getClass();

            assertEquals(expectedType, actualType, createProblemMessage(i, expectedType, actualType));

        }
    }

    /**
     * Creates a problem message for the sequence assertions at a given index with the expected and
     * actual type
     * 
     * @param index
     *            Index into the sequence, that was incorrect
     * @param expectedType
     *            Expected class of the sequence at the given index
     * @param actualType
     *            Actual class of the sequence at the given index
     * @return Problem message for the assertion
     */
    private static String createProblemMessage(int index, Class<?> expectedType, Class<?> actualType) {
        return String.format("Type missmatch at index %d. Expected: %s, actual: %s.", index,
                expectedType.getSimpleName(), actualType.getSimpleName());
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} has the entity name of
     * {@code expectedName} and is a SEFF Element
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of
     * different length, they are considered unequal
     * 
     * @param sequence
     *            ActionSequence to be inspected
     * @param index
     *            Index into the given sequence
     * @param expectedName
     *            Expected name at the given {@code index} into the given {@code sequence}
     */
    public static void assertSEFFSequenceElementContent(ActionSequence sequence, int index, String expectedName) {
        assertNotNull(sequence.elements());
        assertTrue(sequence.elements()
            .size() >= index + 1);

        var element = sequence.elements()
            .get(index);

        assertInstanceOf(CallingSEFFActionSequenceElement.class, element);

        var sequenceElement = (CallingSEFFActionSequenceElement) element;
        assertEquals(expectedName, sequenceElement.getElement()
            .getEntityName());
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} has the entity name of
     * {@code expectedName} and is a User Element
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of
     * different length, they are considered unequal
     * 
     * @param sequence
     *            ActionSequence to be inspected
     * @param index
     *            Index into the given sequence
     * @param expectedName
     *            Expected name at the given {@code index} into the given {@code sequence}
     */
    public static void assertUserSequenceElementContent(ActionSequence sequence, int index, String expectedName) {
        assertNotNull(sequence.elements());
        assertTrue(sequence.elements()
            .size() >= index + 1);

        var element = sequence.elements()
            .get(index);

        assertInstanceOf(CallingUserActionSequenceElement.class, element);

        var sequenceElement = (CallingUserActionSequenceElement) element;
        assertEquals(expectedName, sequenceElement.getElement()
            .getEntityName());
    }

}
