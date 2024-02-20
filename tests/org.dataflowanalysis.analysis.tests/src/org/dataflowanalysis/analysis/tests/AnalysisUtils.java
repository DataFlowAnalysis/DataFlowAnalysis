package org.dataflowanalysis.analysis.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFPCMVertex;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserPCMVertex;

public class AnalysisUtils {

    private AnalysisUtils() {
        // Utility class
    }

    public static final String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.testmodels";

    /**
     * <em>Assert</em> that {@code sequence} at index {@code index} and {@code expectedType} are of the same class.
     * <p>
     * If {@code sequence} are {@code null}, they are considered unequal
     * @param sequence ActionSequence to be inspected
     * @param index Index into the {@code sequence} to be compared
     * @param expectedType Expected type of the given ActionSequence at the given index
     */
    public static void assertSequenceElement(AbstractPartialFlowGraph sequence, int index, Class<?> expectedType) {
        assertNotNull(sequence.getVertices());
        assertTrue(sequence.getVertices().size() >= index + 1);

        Class<?> actualType = sequence.getVertices().get(index).getClass();

        assertEquals(expectedType, actualType, createProblemMessage(index, expectedType, actualType));
    }

    /**
     * <em>Assert</em> that the elements in {@code sequence} and {@code expectedType} are ordered like the provided list of
     * classes
     * <p>
     * If {@code sequence} is {@code null} or the sequences are of different length, they are considered unequal
     * @param sequence ActionSequence to be inspected
     * @param expectedElementTypes Expected types of the given ActionSequence at all indexes
     */
    public static void assertSequenceElements(AbstractPartialFlowGraph sequence, List<Class<?>> expectedElementTypes) {
        var elements = sequence.getVertices();

        assertNotNull(elements);
        assertEquals(expectedElementTypes.size(), sequence.getVertices().size());

        for (int i = 0; i < expectedElementTypes.size(); i++) {
            assertSequenceElement(sequence, i, expectedElementTypes.get(i));
        }
    }

    /**
     * Creates a problem message for the sequence assertions at a given index with the expected and actual type
     * @param index Index into the sequence, that was incorrect
     * @param expectedType Expected class of the sequence at the given index
     * @param actualType Actual class of the sequence at the given index
     * @return Problem message for the assertion
     */
    private static String createProblemMessage(int index, Class<?> expectedType, Class<?> actualType) {
        return String.format("Type mismatch at index %d. Expected: %s, actual: %s.", index, expectedType.getSimpleName(), actualType.getSimpleName());
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} has the entity name of {@code expectedName} and is a
     * SEFF Element
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of different length, they are
     * considered unequal
     * @param sequence ActionSequence to be inspected
     * @param index Index into the given sequence
     * @param expectedName Expected name at the given {@code index} into the given {@code sequence}
     */
    public static void assertSEFFSequenceElementContent(AbstractPartialFlowGraph sequence, int index, String expectedName) {
        assertNotNull(sequence.getVertices());
        assertTrue(sequence.getVertices().size() >= index + 1);

        var element = sequence.getVertices().get(index);

        assertInstanceOf(CallingSEFFPCMVertex.class, element);

        var sequenceElement = (CallingSEFFPCMVertex) element;
        assertEquals(expectedName, sequenceElement.getReferencedElement().getEntityName());
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} has the entity name of {@code expectedName} and is a
     * User Element
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of different length, they are
     * considered unequal
     * @param sequence ActionSequence to be inspected
     * @param index Index into the given sequence
     * @param expectedName Expected name at the given {@code index} into the given {@code sequence}
     */
    public static void assertUserSequenceElementContent(AbstractPartialFlowGraph sequence, int index, String expectedName) {
        assertNotNull(sequence.getVertices());
        assertTrue(sequence.getVertices().size() >= index + 1);

        var element = sequence.getVertices().get(index);

        assertInstanceOf(CallingUserPCMVertex.class, element);

        var sequenceElement = (CallingUserPCMVertex) element;
        assertEquals(expectedName, sequenceElement.getReferencedElement().getEntityName());
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} has the given characteristic type and value.
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of different length, they are
     * considered unequal
     * @param sequence ActionSequence to be inspected
     * @param index Index into the given sequence
     * @param variableName Name of the DataFlow variable at the given {@code index}
     * @param characteristicType Expected characteristic type at the given {@code index}
     * @param characteristicValue Expected characteristic value at the given {@code index}
     */
    public static void assertCharacteristicPresent(AbstractPartialFlowGraph sequence, int index, String variableName, String characteristicType,
            String characteristicValue) {
        var sequenceElement = sequence.getVertices().get(index);
        var dataflowVariable = sequenceElement.getAllDataFlowVariables().stream().filter(it -> it.variableName().equals(variableName)).findAny();

        if (dataflowVariable.isEmpty()) {
            fail(String.format("Did not find dataflow variable with name %s at sequence element %s", variableName, sequenceElement));
        }

        var result = dataflowVariable.get().characteristics().stream().filter(it -> it.getTypeName().equals(characteristicType))
                .filter(it -> it.getValueName().equals(characteristicValue)).findAny();

        if (result.isEmpty()) {
            fail(String.format("Could not find dataflow variable %s.%s.%s at sequence element %s", variableName, characteristicType,
                    characteristicValue, sequenceElement));
        }
    }

    /**
     * <em>Assert</em> that {@code sequence} at the given {@code index} does not have the given characteristic type and
     * value.
     * <p>
     * If both {@code sequence} or {@code expectedName} are {@code null} or the sequences are of different length, they are
     * considered unequal
     * @param sequence ActionSequence to be inspected
     * @param index Index into the given sequence
     * @param variableName Name of the DataFlow variable at the given {@code index}
     * @param characteristicType Expected characteristic type at the given {@code index}
     * @param characteristicValue Expected characteristic value at the given {@code index}
     */
    public static void assertCharacteristicAbsent(AbstractPartialFlowGraph sequence, int index, String variableName, String characteristicType,
            String characteristicValue) {
        if (sequence.getVertices().size() < index) {
            fail("Action sequence with length " + sequence.getVertices().size() + " is not long enough for index " + index);
        }
        var sequenceElement = sequence.getVertices().get(index);
        var dataflowVariable = sequenceElement.getAllDataFlowVariables().stream().filter(it -> it.variableName().equals(variableName)).findAny();
        if (dataflowVariable.isEmpty()) {
            return;
        }
        assertTrue(dataflowVariable.get().characteristics().stream().filter(it -> it.getTypeName().equals(characteristicType))
                .filter(it -> it.getValueName().equals(characteristicValue)).findAny().isEmpty());
    }
}
