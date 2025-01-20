package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.core.AbstractVertex;

import java.util.List;

public class DummyVertex extends AbstractVertex<String> {
    private boolean evaluated;
    private final List<DummyVertex> previousElements;

    public DummyVertex(String referencedElement) {
        super(referencedElement);
        this.previousElements = List.of();
        this.evaluated = false;
    }

    public DummyVertex(String referencedElement, List<DummyVertex> previousElements) {
        super(referencedElement);
        this.previousElements = previousElements;
        this.evaluated = false;
    }

    public static DummyVertex of(String referencedElement) {
        return new DummyVertex(referencedElement);
    }

    public static DummyVertex of(String referencedElement, List<DummyVertex> previousElements) {
        return new DummyVertex(referencedElement, previousElements);
    }

    @Override
    public void evaluateDataFlow() {
        this.evaluated = true;
        this.getPreviousElements().forEach(AbstractVertex::evaluateDataFlow);
    }

    @Override
    public String toString() {
        return this.referencedElement;
    }

    @Override
    public List<? extends AbstractVertex<?>> getPreviousElements() {
        return this.previousElements;
    }

    public DummyVertex copy() {
        return new DummyVertex(this.referencedElement + "_copy", this.previousElements);
    }

    @Override
    public boolean isEvaluated() {
        return evaluated;
    }
}
