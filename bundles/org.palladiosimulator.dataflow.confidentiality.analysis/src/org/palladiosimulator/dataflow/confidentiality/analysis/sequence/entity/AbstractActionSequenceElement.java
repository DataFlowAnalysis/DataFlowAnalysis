package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.palladiosimulator.dataflow.dictionary.characterized.DataDictionaryCharacterized.Literal;

public abstract class AbstractActionSequenceElement<T extends EObject> {

    private final Optional<List<DataFlowVariable>> dataFlowVariables;
    private final Optional<List<CharacteristicValue>> nodeCharacteristics;

    public AbstractActionSequenceElement() {
        this.dataFlowVariables = Optional.empty();
        this.nodeCharacteristics = Optional.empty();
    }

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
     * See {@link getDataFlowCharacteristicsWithName} for a simular method for DataFlowCharacteristics 
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
     * Returns a list of characteristic literals that are set for a given characteristic type in the list of all data flow variables
     * <p>
     * See {@link getNodeCharacteristicsWithName} for a simular method for NodeCharacteristics
     * @param name Name of the characteristic type
     * @return Returns a list of all characteristic literals matching the characteristic type
     */
    public List<Literal> getDataFlowCharacteristicsWithName(String name) {
    	return this.getAllDataFlowVariables().stream()
    			.flatMap(df -> df.getAllCharacteristics().stream())
    			.filter(cv -> cv.characteristicType().getName().equals(name))
    			.map(cv -> cv.characteristicLiteral())
    			.collect(Collectors.toList());
    }
    
    public List<DataFlowVariable> getAllDataFlowVariables() {
        return this.dataFlowVariables.orElseThrow(IllegalStateException::new);
    }
    
    public List<CharacteristicValue> getAllNodeCharacteristics() {
    	return this.nodeCharacteristics.orElseThrow(IllegalStateException::new);
    }

    public boolean isEvaluated() {
        return this.dataFlowVariables.isPresent();
    }

    @Override
    public abstract String toString();

}
