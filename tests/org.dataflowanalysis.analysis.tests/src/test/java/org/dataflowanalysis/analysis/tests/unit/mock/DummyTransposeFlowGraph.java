package org.dataflowanalysis.analysis.tests.unit.mock;

import java.util.List;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;

public class DummyTransposeFlowGraph extends AbstractTransposeFlowGraph {
    private boolean evaluated;

    public DummyTransposeFlowGraph(AbstractVertex<?> sink) {
        super(sink);
        this.evaluated = false;
    }

    public static DummyTransposeFlowGraph of(DummyVertex sink) {
        return new DummyTransposeFlowGraph(sink);
    }

    public static DummyTransposeFlowGraph of(String... vertices) {
        if (vertices.length == 0) {
            return new DummyTransposeFlowGraph(null);
        }
        var previousVertex = DummyVertex.of(vertices[vertices.length - 1]);
        for (int i = vertices.length - 2; i >= 0; i--) {
            previousVertex = DummyVertex.of(vertices[i], List.of(previousVertex));
        }
        return new DummyTransposeFlowGraph(previousVertex);
    }

    @Override
    public AbstractTransposeFlowGraph evaluate() {
        this.evaluated = true;
        this.sink.evaluateDataFlow();
        return this;
    }

    @Override
    public AbstractTransposeFlowGraph copy() {
        DummyVertex sink = (DummyVertex) this.sink;
        return new DummyTransposeFlowGraph(sink.copy());
    }

    public boolean isEvaluated() {
        return evaluated;
    }
}
