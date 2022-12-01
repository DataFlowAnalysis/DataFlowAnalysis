package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DatabaseActionSequenceElement;

public record ActionSequence(List<AbstractActionSequenceElement<?>> elements) implements Comparable<ActionSequence> {
	private static final Logger logger = Logger.getLogger(ActionSequence.class);
	
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
    	// TODO: Save variable frames for each Calling Element
    	// TODO On Call: Duplicate current Variable Frame
    	// TODO: On Return: Reuse old Variable Frame, and add RETURN Dataflow Variable
        var iterator = this.elements()
            .iterator();
        Deque<List<DataFlowVariable>> variableContexts = new ArrayDeque<>();
        variableContexts.push(new ArrayList<>());
        
        List<AbstractActionSequenceElement<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractActionSequenceElement<?> nextElement = iterator.next();
            
            if (nextElement instanceof CallReturnBehavior && ((CallReturnBehavior) nextElement).isReturning()) {
            	// Returning from a method me need to look for the RETURN DataFlowVariable save it in the lower variable context, and discard the current one
            	List<DataFlowVariable> returningDataFlowVariables = variableContexts.peek().stream()
            			.filter(it -> it.variableName().equals("RETURN"))
            			.collect(Collectors.toList());
            	variableContexts.pop();
            	returningDataFlowVariables.addAll(variableContexts.peek());
            	variableContexts.pop();
            	variableContexts.push(returningDataFlowVariables);
            }
            
            AbstractActionSequenceElement<?> evaluatedElement = nextElement.evaluateDataFlow(variableContexts.peek());

            evaluatedElements.add(evaluatedElement);
            
            if (evaluatedElement instanceof CallReturnBehavior && ((CallReturnBehavior) evaluatedElement).isCalling()) {
            	// Calling a method, we need to create a new variable context with the existing variables
            	List<DataFlowVariable> callingDataFlowVariables = new ArrayList<>(evaluatedElement.getAllDataFlowVariables());
            	variableContexts.push(callingDataFlowVariables);
            } else {
            	// If no new context is required, replace current variable context
            	variableContexts.pop();
            	variableContexts.push(evaluatedElement.getAllDataFlowVariables());
            }
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
				logger.error("Found incompatible set of Databases, Action Sequences depend on each other");
				throw new IllegalStateException("Cylic loop of databases found in action sequences");
			}
		}
	}
}
