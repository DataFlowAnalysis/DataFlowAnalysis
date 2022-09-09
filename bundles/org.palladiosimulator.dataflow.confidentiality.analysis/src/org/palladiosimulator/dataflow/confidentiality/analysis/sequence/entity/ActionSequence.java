package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public record ActionSequence(List<AbstractActionSequenceElement<?>> elements) {

    public ActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        this.elements = List.copyOf(elements);
    }

    public ActionSequence() {
        this(List.of());
    }

    public ActionSequence(ActionSequence sequence) {
        this(sequence.elements());
    }

    public ActionSequence(AbstractActionSequenceElement<?>... elements) {
        this(List.of(elements));
    }

    public ActionSequence(ActionSequence sequence, AbstractActionSequenceElement<?>... newElements) {
        this(Stream.concat(sequence.elements()
            .stream(), Stream.of(newElements))
            .toList());
    }

    public ActionSequence evaluateDataFlow() {
        var iterator = this.elements()
            .iterator();
        List<DataFlowVariable> currentVariables = new ArrayList<>();
        List<AbstractActionSequenceElement<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractActionSequenceElement<?> nextElement = iterator.next();
            AbstractActionSequenceElement<?> evaluatedElement = nextElement.evaluateDataFlow(currentVariables);

            evaluatedElements.add(evaluatedElement);
            currentVariables = evaluatedElement.getAllDataFlowVariables();
        }

        return new ActionSequence(evaluatedElements);
    }

    @Override
    public String toString() {
        return this.elements()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
