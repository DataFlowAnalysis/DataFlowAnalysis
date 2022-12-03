package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.PCMAnalysisUtils;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.NodeCharacteristicsCalculator;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.allocation.AllocationPackage;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public class DatabaseActionSequenceElement<T extends OperationalDataStoreComponent> extends AbstractPCMActionSequenceElement<T> {
	private final Logger logger = Logger.getLogger(DatabaseActionSequenceElement.class);
	
	private DataStore dataStore;
	private boolean isWriting;
	
	/**
	 * Create a new Database Action Sequence Element with the underlying Palladio Element, Assembly Context, DataStore and indication wheter the Data Store is written to
	 * @param element Underlying Palladio Element
	 * @param context Assembly Context of the SEFF 
	 * @param isWriting Is true, if the data store is written to. Otherwise, the data store is read from
	 * @param dataStore Reference to the data store that is attached to this Database Action Sequence Element
	 */
	public DatabaseActionSequenceElement(T element, Deque<AssemblyContext> context, boolean isWriting, DataStore dataStore) {
        super(element, context);
        this.isWriting = isWriting;
        this.dataStore = dataStore;
    }
	
	/**
	 * Constructs a new Database Action Sequence Element given an old Database Action Sequence Element and an updated List of dataflow variables and Node characteristics
	 * @param oldElement Old Database Action Sequence element, which attributes are copied
	 * @param dataFlowVariables Updated list of dataflow variables
	 * @param nodeCharacteristics Updated list of node characteristics
	 */
	public DatabaseActionSequenceElement(DatabaseActionSequenceElement<T> oldElement, 
			List<DataFlowVariable> dataFlowVariables, 
			List<CharacteristicValue> nodeCharacteristics) {
		super(oldElement, dataFlowVariables, nodeCharacteristics);
		this.isWriting = oldElement.isWriting();
		this.dataStore = oldElement.getDataStore();
	}

	@Override
	public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables) {
		List<CharacteristicValue> nodeVariables = this.evaluateNodeCharacteristics();
		List<DataFlowVariable> newDataFlowVariables = new ArrayList<>(variables);
		
		if (this.isWriting()) {
			String dataSourceName = this.getDataStore().getDatabaseVariableName().get();
			DataFlowVariable dataSource = newDataFlowVariables.stream()
					.filter(it -> it.variableName().equals(dataSourceName))
					.findAny().orElse(new DataFlowVariable(dataSourceName));
			dataStore.addCharacteristicValues(dataSource.characteristics());
			return new DatabaseActionSequenceElement<>(this, newDataFlowVariables, nodeVariables);
		}
		
		if (this.dataStore.getCharacteristicValues().isEmpty() || this.dataStore.getDatabaseVariableName().isEmpty()) {
			logger.warn("Database " +  this.dataStore.getDatabaseComponentName() + " is read from without writing any values to it!");
		}
		
		DataFlowVariable modifiedVariable = new DataFlowVariable("RETURN");
		List<CharacteristicValue> storedData = dataStore.getCharacteristicValues();
		for(CharacteristicValue characteristicValue : storedData) {
			modifiedVariable = modifiedVariable.addCharacteristic(characteristicValue);
		}
		newDataFlowVariables.add(modifiedVariable);
		return new DatabaseActionSequenceElement<>(this, newDataFlowVariables, nodeVariables);
	}
	
    protected List<CharacteristicValue> evaluateNodeCharacteristics() {
    	NodeCharacteristicsCalculator characteristicsCalculator = new NodeCharacteristicsCalculator(this.getElement());
    	return characteristicsCalculator.getNodeCharacteristics(Optional.of(this.getContext()));
    }
	
	public boolean isWriting() {
		return this.isWriting;
	}
	
	public DataStore getDataStore() {
		return dataStore;
	}

	@Override
	public String toString() {
		String writing = isWriting ? "writing" : "reading";
        return String.format("%s / %s (%s, %s))", this.getClass()
            .getSimpleName(), writing,
                this.getElement()
                    .getEntityName(),
                this.getElement()
                    .getId());
	}
}