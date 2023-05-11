package org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.seff;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.CharacteristicValue;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataFlowVariable;
import org.palladiosimulator.dataflow.confidentiality.analysis.characteristics.DataStore;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.AbstractPCMActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.repository.OperationalDataStoreComponent;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;

public class DatabaseActionSequenceElement<T extends OperationalDataStoreComponent> extends AbstractPCMActionSequenceElement<T> {
	private static final String RETURN_MAGIC_VALUE = "RETURN";
	
	private final Logger logger = Logger.getLogger(DatabaseActionSequenceElement.class);
	
	private final DataStore dataStore;
	private final boolean isWriting;
	
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
	 * @param dataFlowVariables Updated list of dataflow variable
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
	public AbstractActionSequenceElement<T> evaluateDataFlow(List<DataFlowVariable> variables, AnalysisData analysisData) {
		List<CharacteristicValue> nodeVariables = super.getNodeCharacteristics(analysisData);
		List<DataFlowVariable> newDataFlowVariables = new ArrayList<>(variables);
	
		if (!this.isWriting && (this.dataStore.getCharacteristicValues().isEmpty() || this.dataStore.getDatabaseVariableName().isEmpty())) {
			logger.warn("Database " +  this.dataStore.getDatabaseComponentName() + " is read from without writing any values to it!");
		}
		
		if (this.isWriting()) {
			String dataSourceName = this.getDataStore().getDatabaseVariableName().get();
			DataFlowVariable dataSource = newDataFlowVariables.stream()
					.filter(it -> it.variableName().equals(dataSourceName))
					.findAny().orElse(new DataFlowVariable(dataSourceName));
			dataStore.addCharacteristicValues(dataSource.characteristics());
			logger.trace(this.createPrintableDatabaseInformation(newDataFlowVariables));
			return new DatabaseActionSequenceElement<>(this, newDataFlowVariables, nodeVariables);
		} else {
			DataFlowVariable modifiedVariable = new DataFlowVariable(DatabaseActionSequenceElement.RETURN_MAGIC_VALUE);
			List<CharacteristicValue> storedData = dataStore.getCharacteristicValues();
			for(CharacteristicValue characteristicValue : storedData) {
				modifiedVariable = modifiedVariable.addCharacteristic(characteristicValue);
			}
			newDataFlowVariables.add(modifiedVariable);
			logger.trace(this.createPrintableDatabaseInformation(List.of(modifiedVariable)));
		}
		return new DatabaseActionSequenceElement<>(this, newDataFlowVariables, nodeVariables);
	}
	
	/**
	 * Returns whether the data store is written to
	 * @return Returns true, if the data store is written to. Otherwise, the method returns false
	 */
	public boolean isWriting() {
		return this.isWriting;
	}
	
	/**
	 * Returns the data store of the sequence element
	 * @return Data store of the sequence element
	 */
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
	
	/**
	 * Creates printable information for the database component
	 * @param variables
	 * @return
	 */
	public String createPrintableDatabaseInformation(List<DataFlowVariable> variables) {
		String writing = isWriting ? "Writing DataFlowVariables: %s to" : "Reading DataFlowVariables: %s from";
		String dataCharacteristics = variables
	            .stream()
	            .map(e -> String.format("%s [%s]", e.variableName(),
	                    createPrintableCharacteristicsList(e.getAllCharacteristics())))
	            .collect(Collectors.joining(", "));
		String changedDataFlowVariables = String.format(writing, dataCharacteristics);
		return String.format("%s node of Database Component %s", changedDataFlowVariables, this.getDataStore().getDatabaseComponentName());
	}
}
