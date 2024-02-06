package org.dataflowanalysis.analysis.flowgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataFlowVariable;

/**
 * This class represents an abstract vertex in a {@link AbstractPartialFlowGraph}.
 * An abstract vertex represents an element in a partial flow graph and links to an element.
 * An element referenced in this way may be referenced multiple times by different abstract vertices.
 * Furthermore, the abstract vertex saved incoming and outgoing data flow variables and the characteristics present at the vertex.
 * 
 * @param T Type parameter representing the type of the stored object
 */
public abstract class AbstractVertex<T extends Object> {
	private final Logger logger = Logger.getLogger(AbstractVertex.class);
	
	protected final T referencedElement;
	protected List<? extends AbstractVertex<?>> previousElements;
	
    private Optional<List<DataFlowVariable>> incomingDataFlowVariables;
    private Optional<List<DataFlowVariable>> outgoingDataFlowVariables;
    private Optional<List<CharacteristicValue>> vertexCharacteristics;

    /**
     * Constructs a new action sequence element with empty data flow variables and node characteristics
     */
    public AbstractVertex(T referencedElement, List<? extends AbstractVertex<?>> previousElements) {
    	this.referencedElement = referencedElement;
    	this.previousElements = previousElements;
        this.incomingDataFlowVariables = Optional.empty();
        this.outgoingDataFlowVariables = Optional.empty();
        this.vertexCharacteristics = Optional.empty();
    }
    
    public abstract void evaluateDataFlow();
    
    /**
     * Sets the propagation result of the Vertex to the given result. 
     * This method should only be called once on elements that are not evaluated.
     * @param incomingDataFlowVariables Incoming data flow variables that flow into the vertex
     * @param outgoingDataFlowVariables Outgoing data flow variables that flow out of the vertex
     * @param vertexCharacteristics Vertex characteristics present at the node
     */
    protected void setPropagationResult(List<DataFlowVariable> incomingDataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> vertexCharacteristics) {
    	if (this.isEvaluated()) {
    		logger.error("Cannot set propagation result of already evaluated vertex");
    		throw new IllegalArgumentException();
    	}
    	this.incomingDataFlowVariables = Optional.of(new ArrayList<>(incomingDataFlowVariables));
    	this.outgoingDataFlowVariables = Optional.of(new ArrayList<>(outgoingDataFlowVariables));
    	this.vertexCharacteristics = Optional.of(new ArrayList<>(vertexCharacteristics));
    }
    
    /**
     * Returns whether the action sequence element has been evaluated
     * @return Returns true, if the node is evaluated. Otherwise, the method returns false
     */
    public boolean isEvaluated() {
    	return this.incomingDataFlowVariables.isPresent() && this.outgoingDataFlowVariables.isPresent() && this.vertexCharacteristics.isPresent();
    }
    
    public T getReferencedElement() {
		return referencedElement;
	}
    
    /**
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all node characteristics
     * <p>
     * See {@link getDataFlowCharacteristicsWithName} for a similar method for dataflow variables 
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<String> getNodeCharacteristicNamesWithType(String name) {
    	return this.getAllNodeCharacteristics().stream()
		.filter(cv -> cv.getTypeName().equals(name))
		.map(cv -> cv.getValueName())
		.collect(Collectors.toList());
    }
    
    /**
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all node characteristics
     * <p>
     * See {@link getDataFlowCharacteristicsWithName} for a similar method for dataflow variables 
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<String> getNodeCharacteristicIdsWithType(String name) {
    	return this.getAllNodeCharacteristics().stream()
		.filter(cv -> cv.getTypeName().equals(name))
		.map(cv -> cv.getValueId())
		.collect(Collectors.toList());
    }
    
    /**
     * Returns a List of ids of characteristics and dataflow variables that are set for a given characteristic type in the list of all data flow variables
     * <p>
     * See {@link getNodeCharacteristicIdsWithType} for a similar method for node characteristics
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<List<String>> getDataFlowCharacteristicIdsWithType(String type) {
    	List<List<String>> dfCharIds = new ArrayList<>();
    	for(DataFlowVariable df : this.getAllDataFlowVariables()) {
    		List<String> charValueIds = new ArrayList<>();
    		for(CharacteristicValue charValue : df.getAllCharacteristics()) {
    			if(charValue.getTypeName().equals(type)) {
    				charValueIds.add(charValue.getValueId());
    			}
    		}
    		dfCharIds.add(charValueIds);
    	}
    	
    	return dfCharIds;
    }
    
    /**
     * Returns a List of names of characteristics and dataflow variables that are set for a given characteristic type in the list of all data flow variables
     * <p>
     * See {@link getNodeCharacteristicNamesWithType} for a similar method for node characteristics
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<List<String>> getDataFlowCharacteristicNamesWithType(String type) {
    	List<List<String>> dfCharIds = new ArrayList<>();
    	for(DataFlowVariable df : this.getAllDataFlowVariables()) {
    		List<String> charValueIds = new ArrayList<>();
    		for(CharacteristicValue charValue : df.getAllCharacteristics()) {
    			if(charValue.getTypeName().equals(type)) {
    				charValueIds.add(charValue.getValueName());
    			}
    		}
    		dfCharIds.add(charValueIds);
    	}
    	
    	return dfCharIds;
    }
    
    /**
     * Returns a list of all dataflow variables that are present for the action sequence element
     * @return List of present dataflow variables
     */
    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.incomingDataFlowVariables.orElseThrow(IllegalStateException::new);
    }
    
    public List<DataFlowVariable> getAllIncomingDataFlowVariables() {
		return this.incomingDataFlowVariables.orElseThrow(IllegalStateException::new);
	}
    
    /**
     * Returns a list of all outgoing dataflow variables that are present for the action sequence element
     * @return List of present outgoing dataflow variables (e.g. the variables at the output pin of the DFD representation)
     */
    public List<DataFlowVariable> getAllOutgoingDataFlowVariables() {
        return this.outgoingDataFlowVariables.orElseThrow(IllegalStateException::new);
    }
    
    /**
     * Returns a list of all present node characteristics for the action sequence element
     * @return List of present node characteristics
     */
    public List<CharacteristicValue> getAllNodeCharacteristics() {
    	return this.vertexCharacteristics.orElseThrow(IllegalStateException::new);
    }
    
    public List<? extends AbstractVertex<?>> getPreviousElements() {
		return this.previousElements;
	}
    
    public boolean isSource() {
    	return this.previousElements.isEmpty();
    }

    @Override
    public abstract String toString();
    
    /**
     * Returns a string with detailed information about a node's characteristics, data flow
     * variables and the variables' characteristics.
     * 
     * @param node
     *            a sequence element after the label propagation happened
     * @return a string with the node's string representation and a list of all related
     *         characteristics types and literals
     */
    public String createPrintableNodeInformation() {
        String template = "Propagated %s%s\tNode characteristics: %s%s\tData flow Variables:  %s%s";
        String nodeCharacteristics = createPrintableCharacteristicsList(this.getAllNodeCharacteristics());
        String dataCharacteristics = this.getAllDataFlowVariables()
            .stream()
            .map(e -> String.format("%s [%s]", e.variableName(),
                    createPrintableCharacteristicsList(e.getAllCharacteristics())))
            .collect(Collectors.joining(", "));

        return String.format(template, this.toString(), System.lineSeparator(), nodeCharacteristics,
                System.lineSeparator(), dataCharacteristics, System.lineSeparator());
    }

    /**
     * Returns a string with the names of all characteristic types and selected literals of all
     * characteristic values.
     * 
     * @param characteristics
     *            a list of characteristics values
     * @return a comma separated list of the format "type.literal, type.literal"
     */
    public String createPrintableCharacteristicsList(List<CharacteristicValue> characteristics) {
        List<String> entries = characteristics.stream()
                .map(it -> String.format("%s.%s", it.getTypeName(),
                        it.getValueName()))
                .toList();
            return String.join(", ", entries);
    }
    
    public void setPreviousElements(List<? extends AbstractVertex<?>> previousElements) {
		this.previousElements = previousElements;
	}
}
