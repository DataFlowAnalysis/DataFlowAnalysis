package org.palladiosimulator.dataflow.confidentiality.analysis.entity;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;

public abstract class ActionSequence {
	protected List<AbstractActionSequenceElement<?>> elements;
	
	public ActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        this.elements = List.copyOf(elements);
    }
    
    public abstract ActionSequence evaluateDataFlow(PCMResourceLoader resourceLoader);
    
    public List<AbstractActionSequenceElement<?>> getElements() {
		return elements;
	}
    @Override
    public String toString() {
        return this.getElements()
            .stream()
            .map(it -> it.toString())
            .reduce("", (t, u) -> String.format("%s%s%s", t, System.lineSeparator(), u));
    }
}
