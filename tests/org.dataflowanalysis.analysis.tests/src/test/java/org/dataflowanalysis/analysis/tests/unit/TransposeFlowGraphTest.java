package org.dataflowanalysis.analysis.tests.unit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyTransposeFlowGraph;
import org.dataflowanalysis.analysis.tests.unit.mock.DummyVertex;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TransposeFlowGraphTest {
    private static Stream<Arguments> transposeFlowGraphs() {
        var vertexB = DummyVertex.of("B");
        var vertexC = DummyVertex.of("C");
        var vertexA = DummyVertex.of("A", List.of(vertexB, vertexC));
        return Stream.of(Arguments.of(DummyTransposeFlowGraph.of("1"), 1), Arguments.of(DummyTransposeFlowGraph.of("1", "2", "3"), 3),
                Arguments.of(DummyTransposeFlowGraph.of(vertexA), 3));
    }

    private static Stream<Arguments> succeedingVerticesTransposeFlowGraph() {
        var vertexB = DummyVertex.of("B");
        var vertexC = DummyVertex.of("C");
        var vertexA = DummyVertex.of("A", List.of(vertexB, vertexC));
        return Stream.of(Arguments.of(DummyTransposeFlowGraph.of(vertexA), vertexB, List.of(vertexA)),
                Arguments.of(DummyTransposeFlowGraph.of(vertexA), vertexC, List.of(vertexA)),
                Arguments.of(DummyTransposeFlowGraph.of(vertexA), vertexA, List.of()));
    }

    @ParameterizedTest
    @MethodSource("transposeFlowGraphs")
    public void shouldEvaluateCorrectly(DummyTransposeFlowGraph transposeFlowGraph) {
        transposeFlowGraph.evaluate();
        assertTrue(transposeFlowGraph.isEvaluated());
        for (var vertex : transposeFlowGraph.getVertices()) {
            assertTrue(vertex.isEvaluated());
        }
    }

    @ParameterizedTest
    @MethodSource("transposeFlowGraphs")
    public void shouldCopyCorrectly(DummyTransposeFlowGraph transposeFlowGraph) {
        assertNotEquals(transposeFlowGraph, transposeFlowGraph.copy());
    }

    @ParameterizedTest
    @MethodSource("transposeFlowGraphs")
    public void shouldStoreCorrectly(DummyTransposeFlowGraph transposeFlowGraph, int expectedSize) {
        assertFalse(transposeFlowGraph.getVertices()
                .isEmpty());
        assertNotNull(transposeFlowGraph.getSink());
        assertEquals(expectedSize, transposeFlowGraph.getVertices()
                .size());
    }

    @ParameterizedTest
    @MethodSource("transposeFlowGraphs")
    public void shouldAccessCorrectly(DummyTransposeFlowGraph transposeFlowGraph, int expectedSize) {
        assertEquals(expectedSize, transposeFlowGraph.getVertices()
                .size());
        assertEquals(expectedSize, transposeFlowGraph.stream()
                .toList()
                .size());
        assertIterableEquals(transposeFlowGraph.getVertices(), transposeFlowGraph.stream()
                .toList());
    }

    @ParameterizedTest
    @MethodSource("succeedingVerticesTransposeFlowGraph")
    public void shouldDeterminePreviousVertex(DummyTransposeFlowGraph transposeFlowGraph, DummyVertex vertex, List<DummyVertex> expectedResult) {
        assertIterableEquals(expectedResult, transposeFlowGraph.getSucceedingVertices(vertex));
    }
}
