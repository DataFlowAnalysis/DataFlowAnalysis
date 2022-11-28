package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DatabaseActionSequenceElement;

public record ActionSequence(List<AbstractActionSequenceElement<?>> elements) implements Comparable<ActionSequence> {

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
    
    public List<String> getProvidedDatabases() {
    	return this.elements.stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> it.isWriting())
				.map(it -> it.getDataStore().getDatabaseComponentName())
				.collect(Collectors.toList());
    }
    
    public List<String> getRequiredDatabases() {
    	return this.elements.stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> !it.isWriting())
				.map(it -> it.getDataStore().getDatabaseComponentName())
				.collect(Collectors.toList());
    }

    /**
     * Return -1, when this sequence needs to be executed before the other
     * Return 0, if the sequences can run simultaniously
     * Return 1, if the other sequence needs to run first
     */
	@Override
	public int compareTo(ActionSequence otherSequence) {
		if (this.getRequiredDatabases().isEmpty() && otherSequence.getRequiredDatabases().isEmpty()) {
			return 0;
		} else if (this.getRequiredDatabases().isEmpty() && !otherSequence.getRequiredDatabases().isEmpty()) {
			return -1;
		} else if (!this.getRequiredDatabases().isEmpty() && otherSequence.getRequiredDatabases().isEmpty()) {
			return 1;
		} else {
			List<String> requriredDatabases = this.getRequiredDatabases();
			List<String> providedDatabases = this.getProvidedDatabases();
			
			List<String> otherRequiredDatabases = otherSequence.getRequiredDatabases();
			List<String> otherProvidedDatabases = otherSequence.getProvidedDatabases();
			
			if (requriredDatabases.containsAll(otherProvidedDatabases)) {
				return 1;
			} else if (otherRequiredDatabases.containsAll(providedDatabases)) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
