package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CallReturnBehavior;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.pcm.seff.StartAction;

public class PCMActionSequence extends ActionSequence implements Comparable<PCMActionSequence> {
	private static final Logger logger = Logger.getLogger(PCMActionSequence.class);
	
	public PCMActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        super(elements);
    }

    public PCMActionSequence() {
        super(List.of());
    }

    public PCMActionSequence(ActionSequence sequence) {
        super(sequence.getElements());
    }

    public PCMActionSequence(AbstractActionSequenceElement<?>... elements) {
        super(List.of(elements));
    }

    public PCMActionSequence(ActionSequence sequence, AbstractActionSequenceElement<?>... newElements) {
        super(Stream.concat(sequence.getElements()
            .stream(), Stream.of(newElements))
            .toList());
    }

	@Override
    public ActionSequence evaluateDataFlow() {
        var iterator = super.getElements()
            .iterator();
        Deque<List<DataFlowVariable>> variableContexts = new ArrayDeque<>();
        variableContexts.push(new ArrayList<>());
        
        List<AbstractActionSequenceElement<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractActionSequenceElement<?> nextElement = iterator.next();
            
            prepareCall(variableContexts, nextElement);
            
            AbstractActionSequenceElement<?> evaluatedElement = nextElement.evaluateDataFlow(variableContexts.peek());
            evaluatedElements.add(evaluatedElement);
            
            cleanupCall(variableContexts, evaluatedElement);
        }

        return new PCMActionSequence(evaluatedElements);
    }
	
	/**
	 * Prepares the stack of variable contexts to contain the correct entry at the top, by updating elements, if the next element is returning
	 * @param variableContexts Stack of variable contexts
	 * @param nextElement Next element that will be evaluated
	 */
	private void prepareCall(Deque<List<DataFlowVariable>> variableContexts, AbstractActionSequenceElement<?> nextElement) {
		if (nextElement instanceof SEFFActionSequenceElement<?> && ((SEFFActionSequenceElement<?>) nextElement).getElement() instanceof StartAction) {
			SEFFActionSequenceElement<?> startElement = (SEFFActionSequenceElement<?>) nextElement;
			List<String> parameter = startElement.getParameter().stream()
					.map(it -> it.getParameterName())
					.collect(Collectors.toList());
			
			List<DataFlowVariable> presentDataFlowVariables = variableContexts.peek().stream()
					.filter(it -> parameter.contains(it.variableName()))
					.collect(Collectors.toList());
			variableContexts.pop();
			variableContexts.push(presentDataFlowVariables);
		}
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
	}
	
	/**
	 * Prepares the stack of variable contexts to contain the correct entry at the top, after a node has been evaluated.
	 * @param variableContexts Stack of variable contexts
	 * @param evaluatedElement Element that has been evaluated
	 */
	private void cleanupCall(Deque<List<DataFlowVariable>> variableContexts, 
			AbstractActionSequenceElement<?> evaluatedElement) {
		if (evaluatedElement instanceof CallReturnBehavior && ((CallReturnBehavior) evaluatedElement).isCalling()) {
        	List<DataFlowVariable> callingDataFlowVariables = new ArrayList<>(evaluatedElement.getAllDataFlowVariables());
        	variableContexts.push(callingDataFlowVariables);
        } else if (evaluatedElement instanceof CallReturnBehavior && ((CallReturnBehavior) evaluatedElement).isReturning()) {
        	List<DataFlowVariable> returingDataFlowVariables = evaluatedElement.getAllDataFlowVariables().stream()
        			.filter(it -> !it.variableName().equals("RETURN"))
        			.collect(Collectors.toList());
        	variableContexts.pop();
        	variableContexts.push(returingDataFlowVariables);
        } else  {
        	variableContexts.pop();
        	variableContexts.push(evaluatedElement.getAllDataFlowVariables());
        }
	}
	
    
    public List<String> getProvidedDatabases() {
    	List<DatabaseActionSequenceElement<?>> potentialProvided = this.getElements().stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> it.isWriting())
				.collect(Collectors.toList());
    	List<DatabaseActionSequenceElement<?>> providedDatabases = potentialProvided.stream()
    			.filter(it -> !getRequiredBefore(it).contains(it.getDataStore().getDatabaseComponentName()))
    			.collect(Collectors.toList());
    	return providedDatabases.stream()
    			.map(it -> it.getDataStore().getDatabaseComponentName())
    			.collect(Collectors.toList());
    }
    
    private List<String> getRequiredBefore(DatabaseActionSequenceElement<?> element) {
    	int index = this.getElements().indexOf(element);
    	return this.getElements().stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> !it.isWriting())
				.limit(index)
				.map(it -> it.getDataStore().getDatabaseComponentName())
				.collect(Collectors.toList());
    }
    
    public List<String> getRequiredDatabases() {
    	List<DatabaseActionSequenceElement<?>> potentialRequired = this.getElements().stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> !it.isWriting())
				.collect(Collectors.toList());
    	List<DatabaseActionSequenceElement<?>> requiredDatabases = potentialRequired.stream()
    			.filter(it -> !getProvidedBefore(it).contains(it.getDataStore().getDatabaseComponentName()))
    			.collect(Collectors.toList());
    	return requiredDatabases.stream()
    			.map(it -> it.getDataStore().getDatabaseComponentName())
    			.collect(Collectors.toList());
    }
    
    private List<String> getProvidedBefore(DatabaseActionSequenceElement<?> element) {
    	int index = this.getElements().indexOf(element);
    	return this.getElements().stream()
				.filter(DatabaseActionSequenceElement.class::isInstance)
				.map(DatabaseActionSequenceElement.class::cast)
				.filter(it -> it.isWriting())
				.limit(index)
				.map(it -> it.getDataStore().getDatabaseComponentName())
				.collect(Collectors.toList());
    }

    /**
     * Return -1, when this sequence needs to be executed before the other
     * Return 0, if the sequences can run simultaneously
     * Return 1, if the other sequence needs to run first
     */
	@Override
	public int compareTo(PCMActionSequence otherSequence) {
		if (this.getRequiredDatabases().isEmpty() && otherSequence.getRequiredDatabases().isEmpty()) {
			return 0;
		} else if (this.getRequiredDatabases().isEmpty() && !otherSequence.getRequiredDatabases().isEmpty()) {
			return -1;
		} else if (!this.getRequiredDatabases().isEmpty() && otherSequence.getRequiredDatabases().isEmpty()) {
			return 1;
		} else {
			logger.error("Found incompatible set of Databases, Action Sequences depend on each other");
			logger.error("Problematic sequences: " + this + ", " + otherSequence);
			throw new IllegalStateException("Cylic loop of databases found in action sequences");
		}
	}
}
