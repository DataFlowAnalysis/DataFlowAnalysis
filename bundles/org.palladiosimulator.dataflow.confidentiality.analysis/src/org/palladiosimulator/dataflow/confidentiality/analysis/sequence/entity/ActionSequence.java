package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayList;
import java.util.List;

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
        // TODO: Find a smarter way to do this. Java is so dumb compared to Scala...
        var allElements = new ArrayList<AbstractActionSequenceElement<?>>();
        
        for (var element : sequence.getElements()) {
            allElements.add(element);
        }
        
        for (var element: newElements) {
            allElements.add(element);
        }
      
        this.elements = allElements;
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
}
