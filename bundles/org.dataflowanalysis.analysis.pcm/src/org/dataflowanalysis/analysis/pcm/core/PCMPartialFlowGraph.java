package org.dataflowanalysis.analysis.pcm.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.DataCharacteristicsCalculatorFactory;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.core.NodeCharacteristicsCalculator;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFPCMVertex;
import org.palladiosimulator.pcm.seff.StartAction;

public class PCMPartialFlowGraph extends AbstractPartialFlowGraph {

	/**
	 * Creates a empty new action sequence
	 */
    public PCMPartialFlowGraph() {
        super(List.of());
    }
	
	/**
	 * Creates a new action sequence with the given elements
	 * @param elements List of elements contained in the sequence
	 */
	public PCMPartialFlowGraph(List<AbstractVertex<?>> elements) {
        super(elements);
    }

    /**
     * Creates a new action sequence with the given list of elements
     * @param elements Elements that are contained in the sequence
     */
    public PCMPartialFlowGraph(AbstractVertex<?>... elements) {
        super(List.of(elements));
    }

    /**
     * Creates a copy of the given action sequence
     * @param sequence Action sequence that should be copied
     */
    public PCMPartialFlowGraph(AbstractPartialFlowGraph sequence) {
        super(sequence.getVertices());
    }
    
    /**
     * Creates a copy of the given action sequence and appends the given
     * @param sequence Action sequence that should be copied
     * @param newElements Elements in the new sequence
     */
    public PCMPartialFlowGraph(AbstractPartialFlowGraph sequence, AbstractVertex<?>... newElements) {
        super(Stream.concat(sequence.getVertices()
            .stream(), Stream.of(newElements))
            .toList());
    }

	@Override
    public AbstractPartialFlowGraph evaluateDataFlow(NodeCharacteristicsCalculator nodeCharacteristicsCalculator, 
    		DataCharacteristicsCalculatorFactory dataCharacteristicsCalculatorFactory) {
        var iterator = super.getVertices()
            .iterator();
        Deque<List<DataFlowVariable>> variableContexts = new ArrayDeque<>();
        variableContexts.push(new ArrayList<>());
        
        List<AbstractVertex<?>> evaluatedElements = new ArrayList<>();

        while (iterator.hasNext()) {
            AbstractVertex<?> nextElement = iterator.next();
            
            prepareCall(variableContexts, nextElement);
            
            AbstractVertex<?> evaluatedElement = nextElement.evaluateDataFlow(variableContexts.peek(), nodeCharacteristicsCalculator, dataCharacteristicsCalculatorFactory);
            evaluatedElements.add(evaluatedElement);
            
            cleanupCall(variableContexts, evaluatedElement);
        }

        return new PCMPartialFlowGraph(evaluatedElements);
    }
	
	/**
	 * Prepares the stack of variable contexts to contain the correct entry at the top, by updating elements, if the next element is returning
	 * @param variableContexts Stack of variable contexts
	 * @param nextElement Next element that will be evaluated
	 */
	private void prepareCall(Deque<List<DataFlowVariable>> variableContexts, AbstractVertex<?> nextElement) {
		if (nextElement instanceof SEFFPCMVertex<?> && ((SEFFPCMVertex<?>) nextElement).getReferencedElement() instanceof StartAction) {
			SEFFPCMVertex<?> startElement = (SEFFPCMVertex<?>) nextElement;
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
			AbstractVertex<?> evaluatedElement) {
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
