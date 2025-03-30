package org.dataflowanalysis.analysis.tests.unit.mock;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;

import java.util.List;

public class DummyVertex extends AbstractVertex<String> {
    private final List<DummyVertex> previousElements;

    public DummyVertex(String referencedElement) {
        super(referencedElement);
        this.previousElements = List.of();
    }

    public DummyVertex(String referencedElement, List<DummyVertex> previousElements) {
        super(referencedElement);
        this.previousElements = previousElements;
    }

    public static DummyVertex of(String referencedElement) {
        return new DummyVertex(referencedElement);
    }

    public static DummyVertex of(String referencedElement, List<DummyVertex> previousElements) {
        return new DummyVertex(referencedElement, previousElements);
    }

    @Override
    public void evaluateDataFlow() {
        this.setPropagationResult(List.of(), List.of(), List.of());
        this.getPreviousElements().forEach(AbstractVertex::evaluateDataFlow);
    }

    @Override
    public void setPropagationResult(List<DataCharacteristic> incomingDataCharacteristics, List<DataCharacteristic> outgoingDataCharacteristics, List<CharacteristicValue> vertexCharacteristics) {
        super.setPropagationResult(incomingDataCharacteristics, outgoingDataCharacteristics, vertexCharacteristics);
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
}
