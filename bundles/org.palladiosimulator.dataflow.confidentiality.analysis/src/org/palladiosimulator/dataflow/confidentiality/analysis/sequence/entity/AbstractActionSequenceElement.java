package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;
    private final Optional<List<CharacteristicValue>> nodeCharacteristics;

    /**
     * Constructs a new action sequence element with empty dataflow variables and node characteristics
     */
    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
        this.nodeCharacteristics = Optional.empty();
    }

    /**
     * Creates a new action sequence element with updated dataflow variables and node characteristics
     * @param dataFlowVariables List of updated dataflow variables
     * @param nodeCharacteristics List of updated node characteristics
     */
    public AbstractActionSequenceElement(List<DataFlowVariable> dataFlowVariables, List<CharacteristicValue> nodeCharacteristics) {
        this.dataFlowVariables = Optional.of(List.copyOf(dataFlowVariables));
        this.nodeCharacteristics = Optional.of(List.copyOf(nodeCharacteristics));
    }

    /**
     * Evaluates the Data Flow at a given sequence element given the list of {@link DataFlowVariable}s that are received from the precursor
     * @param variables List of {@link DataFlowVariable}s propagated from the precursor
     * @return Returns a new Sequence element with the updated Node- and DataFlowVariables
     */
    public abstract AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables);
    
    /**
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all node characteristics
     * <p>
     * See {@link getDataFlowCharacteristicsWithName} for a similar method for dataflow variables 
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<Literal> getNodeCharacteristicsWithName(String name) {
    	return this.getAllNodeCharacteristics().stream()
		.filter(cv -> cv.characteristicType().getName().equals(name))
		.map(cv -> cv.characteristicLiteral())
		.collect(Collectors.toList());
    }
    
    /**
     * Returns a Map of characteristic literals and dataflow variables that are set for a given characteristic type in the list of all data flow variables
     * <p>
     * See {@link getNodeCharacteristicsWithName} for a similar method for node characteristics
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public Map<DataFlowVariable, List<Literal>> getDataFlowCharacteristicsWithName(String name) {
    	return this.getAllDataFlowVariables().stream()
    			.collect(Collectors.toMap(it -> it, it -> it.characteristics().stream()
    					.filter(df -> df.characteristicType().getName().equals(name))
    					.map(df -> df.characteristicLiteral())
    					.collect(Collectors.toList()))
				);
    }
    
    /**
     * Returns a list of all dataflow variables that are present for the action sequence element
     * @return List of present dataflow variables
     */
    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.dataFlowVariables.orElseThrow(IllegalStateException::new);
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

}
