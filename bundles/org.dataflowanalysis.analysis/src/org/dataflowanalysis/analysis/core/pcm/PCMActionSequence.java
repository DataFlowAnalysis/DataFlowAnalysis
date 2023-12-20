package org.dataflowanalysis.analysis.core.pcm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.pcm.seff.SEFFActionSequenceElement;
import org.palladiosimulator.pcm.seff.StartAction;

public class PCMActionSequence extends ActionSequence {
	private static final Logger logger = Logger.getLogger(PCMActionSequence.class);

	/**
	 * Creates a empty new action sequence
	 */
    public PCMActionSequence() {
        super(List.of());
    }
	
	/**
	 * Creates a new action sequence with the given elements
	 * @param elements List of elements contained in the sequence
	 */
	public PCMActionSequence(List<AbstractActionSequenceElement<?>> elements) {
        super(elements);
    }

    /**
     * Creates a new action sequence with the given list of elements
     * @param elements Elements that are contained in the sequence
     */
    public PCMActionSequence(AbstractActionSequenceElement<?>... elements) {
        super(List.of(elements));
    }

    /**
     * Creates a copy of the given action sequence
     * @param sequence Action sequence that should be copied
     */
    public PCMActionSequence(ActionSequence sequence) {
        super(sequence.getElements());
    }
    
    /**
     * Creates a copy of the given action sequence and appends the given
     * @param sequence Action sequence that should be copied
     * @param newElements Elements in the new sequence
     */
    public PCMActionSequence(ActionSequence sequence, AbstractActionSequenceElement<?>... newElements) {
        super(Stream.concat(sequence.getElements()
            .stream(), Stream.of(newElements))
            .toList());
    }

	@Override
    public ActionSequence evaluateDataFlow(AnalysisData analysisData) {
        var iterator = super.getElements()
            .iterator();
        Deque<List<DataFlowVariable>> variableContexts = new ArrayDeque<>();
        variableContexts.push(new ArrayList<>());
        
        List<AbstractActionSequenceElement<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractActionSequenceElement<?> nextElement = iterator.next();
            
            prepareCall(variableContexts, nextElement);
            
            AbstractActionSequenceElement<?> evaluatedElement = nextElement.evaluateDataFlow(variableContexts.peek(), analysisData);
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
        	List<DataFlowVariable> callingDataFlowVariables = new ArrayList<>(evaluatedElement.getAllOutgoingDataFlowVariables());
        	variableContexts.push(callingDataFlowVariables);
        } else if (evaluatedElement instanceof CallReturnBehavior && ((CallReturnBehavior) evaluatedElement).isReturning()) {
        	List<DataFlowVariable> returingDataFlowVariables = evaluatedElement.getAllOutgoingDataFlowVariables().stream()
        			.filter(it -> !it.variableName().equals("RETURN"))
        			.collect(Collectors.toList());
        	variableContexts.pop();
        	variableContexts.push(returingDataFlowVariables);
        } else  {
        	variableContexts.pop();
        	variableContexts.push(evaluatedElement.getAllOutgoingDataFlowVariables());
        }
	}
}
