package org.dataflowanalysis.analysis.tests.unit;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.tests.unit.mock.CharacteristicsFactory;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyCharacteristicValue;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyVertex;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class VertexTest {
    private static Stream<Arguments> vertices() {
        return Stream.of(
                Arguments.of(DummyVertex.of("A"), true),
                Arguments.of(DummyVertex.of("B", List.of(DummyVertex.of("C", List.of(DummyVertex.of("D"))))), false)
        );
    }

    @ParameterizedTest
    @MethodSource("vertices")
    public void shouldEvaluateCorrectly(DummyVertex vertex) {
        vertex.evaluateDataFlow();
        assertTrue(vertex.isEvaluated());
        for (DummyVertex previous : vertex.getPreviousElements().stream().map(DummyVertex.class::cast).toList()) {
            assertTrue(previous.isEvaluated());
        }
    }

    @Test
    public void shouldSetPropagationResultCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertDoesNotThrow(() -> vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics));
    }

    @Test
    public void shouldNotSetPropagationResultTwice() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }
        assertThrowsExactly(IllegalArgumentException.class, () -> vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics));
    }

    @Test
    public void shouldIndicateEvaluationCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }
        assertTrue(vertex.isEvaluated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "A", "B", "Unicöde", " "})
    public void getReferencedElement(String name) {
        DummyVertex vertex = DummyVertex.of(name);
        assertEquals(name, vertex.getReferencedElement());
    }

    @Test
    public void shouldStoreIncomingCharacteristicsCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals(1, vertex.getAllIncomingDataCharacteristics().size());
        assertEquals("incoming", vertex.getAllIncomingDataCharacteristics().get(0).getVariableName());
        assertEquals(1, vertex.getAllIncomingDataCharacteristics().get(0).getAllCharacteristics().size());
        assertEquals("Type", vertex.getAllIncomingDataCharacteristics().get(0).getAllCharacteristics().get(0).getTypeName());
        assertEquals("Value", vertex.getAllIncomingDataCharacteristics().get(0).getAllCharacteristics().get(0).getValueName());
    }

    @Test
    public void shouldStoreOutgoingCharacteristicsCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals(1, vertex.getAllOutgoingDataCharacteristics().size());
        assertEquals("outgoing", vertex.getAllOutgoingDataCharacteristics().get(0).getVariableName());
        assertEquals(1, vertex.getAllOutgoingDataCharacteristics().get(0).getAllCharacteristics().size());
        assertEquals("Type", vertex.getAllOutgoingDataCharacteristics().get(0).getAllCharacteristics().get(0).getTypeName());
        assertEquals("Value", vertex.getAllOutgoingDataCharacteristics().get(0).getAllCharacteristics().get(0).getValueName());
    }

    @Test
    public void shouldStoreVertexCharacteristicsCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals(1, vertex.getAllVertexCharacteristics().size());
        assertEquals("Type", vertex.getAllVertexCharacteristics().get(0).getTypeName());
        assertEquals("Value", vertex.getAllVertexCharacteristics().get(0).getValueName());
    }

    @ParameterizedTest
    @MethodSource("vertices")
    public void shouldIndicateSourcesCorrectly(AbstractVertex<?> vertex, boolean expected) {
        assertEquals(expected, vertex.isSource());
    }

    @Test
    public void shouldQueryVertexCharacteristicsCorrectly() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals(1, vertex.getVertexCharacteristics("Type").size());
        assertEquals("Value", vertex.getVertexCharacteristics("Type").get(0).getValueName());
        assertEquals(1, vertex.getVertexCharacteristicNames("Type").size());
        assertEquals("Value", vertex.getVertexCharacteristicNames("Type").get(0));
    }

    @Test
    public void shouldQueryCorrectDataCharacteristicsMap() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        assertEquals(1, vertex.getDataCharacteristicMap("Type").size());
        assertTrue(vertex.getDataCharacteristicMap("Type").containsKey("incoming"));
        assertEquals(1, vertex.getDataCharacteristicMap("Type").get("incoming").size());
        assertEquals("Value", vertex.getDataCharacteristicMap("Type").get("incoming").get(0).getValueName());

        assertEquals(1, vertex.getDataCharacteristicNamesMap("Type").size());
        assertTrue(vertex.getDataCharacteristicNamesMap("Type").containsKey("incoming"));
        assertEquals(1, vertex.getDataCharacteristicNamesMap("Type").get("incoming").size());
        assertEquals("Value", vertex.getDataCharacteristicNamesMap("Type").get("incoming").get(0));
    }

    @Test
    public void shouldCreateCorrectPrintableNodeInformation() {
        DummyVertex vertex = DummyVertex.of("A");
        var incomingCharacteristics = List.of(CharacteristicsFactory.of("incoming").with("Type.Value"));
        var outgoingCharacteristics = List.of(CharacteristicsFactory.of("outgoing").with("Type.Value"));
        List<CharacteristicValue> vertexCharacteristics = List.of(DummyCharacteristicValue.fromString("Type.Value"));
        assertFalse(vertex.isEvaluated());
        try {
            vertex.setPropagationResult(incomingCharacteristics, outgoingCharacteristics, vertexCharacteristics);
        } catch (Exception e) {
            fail(e);
        }

        String expected = """
                Propagated A
                \tNode characteristics: Type.Value
                \tData flow Variables:  incoming [Type.Value]
                """;
        assertEquals(expected, vertex.createPrintableNodeInformation());
    }
}
