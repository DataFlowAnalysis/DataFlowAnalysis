package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ActionSequence {

    private final List<AbstractActionSequenceElement<?>> elements;

    public ActionSequence() {
        this.elements = new ArrayList<>();
    }

    public ActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        this.elements = elements;
    }

    public ActionSequence(ActionSequence sequence) {
        this(sequence.getElements());
    }

    public ActionSequence(AbstractActionSequenceElement<?>... elements) {
        this.elements = List.of(elements);
    }

    public ActionSequence(ActionSequence sequence, AbstractActionSequenceElement<?>... newElements) {
        this.elements = Stream.concat(sequence.getElements()
            .stream(), Stream.of(newElements))
            .toList();
    }

    public List<AbstractActionSequenceElement<?>> getElements() {
        return List.copyOf(this.elements);
    }

    public ActionSequence evaluateDataFlow() {
        var iterator = elements.iterator();
        List<DataFlowVariable> currentVariables = new ArrayList<>();
        List<AbstractActionSequenceElement<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractActionSequenceElement<?> nextElement = iterator.next();
            AbstractActionSequenceElement<?> evaluatedElement = nextElement.evaluateDataFlow(currentVariables);

            evaluatedElements.add(evaluatedElement);
            currentVariables = evaluatedElement.getAllDataFlowVariables()
                .get();
        }

        return new ActionSequence(evaluatedElements);
    }

    @Override
    public String toString() {
        return this.getElements()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
