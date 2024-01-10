package org.dataflowanalysis.analysis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.AnalysisData;
import org.eclipse.emf.ecore.EObject;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;
    private final Optional<List<DataFlowVariable>> outgoingDataFlowVariables;
    private final Optional<List<CharacteristicValue>> nodeCharacteristics;

    /**
     * Constructs a new action sequence element with empty dataflow variables and node characteristics
     */
    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
        this.outgoingDataFlowVariables = Optional.empty();
        this.nodeCharacteristics = Optional.empty();
    }

    /**
     * Creates a new action sequence element with updated dataflow variables and node characteristics
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public AbstractActionSequenceElement(List<DataFlowVariable> dataFlowVariables, List<DataFlowVariable> outgoingDataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        this.dataFlowVariables = Optional.of(List.copyOf(dataFlowVariables));
        this.outgoingDataFlowVariables = Optional.of(List.copyOf(outgoingDataFlowVariables));
        this.nodeCharacteristics = Optional.of(List.copyOf(nodeCharacteristics));
    }

    /**
     * Evaluates the Data Flow at a given sequence element given the list of {@link DataFlowVariable}s that are received from the precursor
     * @param variables List of {@link DataFlowVariable}s propagated from the precursor
     * @param analysisData Saved data and calculators of the analysis
     * @return Returns a new Sequence element with the updated Node- and DataFlowVariables
     */
    public abstract AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData);
    
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
        return this.dataFlowVariables.orElseThrow(IllegalStateException::new);
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
    	return this.nodeCharacteristics.orElseThrow(IllegalStateException::new);
    }

    /**
     * Returns whether the action sequence element has been evaluated
     * @return Returns true, if the node is evaluated. Otherwise, the method returns false
     */
    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
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

}
