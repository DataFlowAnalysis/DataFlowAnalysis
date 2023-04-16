package org.palladiosimulator.dataflow.confidentiality.analysis.entity;

import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;

public abstract class ActionSequence {
	protected List<AbstractActionSequenceElement<?>> elements;
	
	public ActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        this.elements = List.copyOf(elements);
    }
    
    public abstract ActionSequence evaluateDataFlow(AnalysisData analysisData);
    
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
