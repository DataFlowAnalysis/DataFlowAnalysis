package org.dataflowanalysis.analysis.characteristics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataStore {
	private final String databaseComponentName;
	private Optional<String> databaseVariableName;
	private final List<CharacteristicValue> characteristicValues;

	/**
	 * Create a new DataStore with a given Database component name, and an empty variable name, which might be unknown at the time of creation
	 * @param databaseComponentName Database component name
	 */
	public DataStore(String databaseComponentName) {
		this.databaseComponentName = databaseComponentName;
		this.databaseVariableName = Optional.empty();
		this.characteristicValues = new ArrayList<>();
	}
	
	/**
	 * Adds the given characteristic values to the list of stored characteristic values
	 * @param characteristicValues The list of added characteristic values
	 */
	public void addCharacteristicValues(List<CharacteristicValue> characteristicValues) {
		this.characteristicValues.addAll(characteristicValues);
	}
	
	/**
	 * Get the name of the underlying database component of the data store
	 * @return Returns the name of the database component
	 */
	public String getDatabaseComponentName() {
		return databaseComponentName;
	}
	
	/**
	 * Sets the variable name, which the data store refers to, as it can be unknown
	 * @param databaseVariableName Database variable name, which is set
	 */
	public void setDatabaseVariableName(String databaseVariableName) {
		this.databaseVariableName = Optional.of(databaseVariableName);
	}
	
	/**
	 * Returns , if known, the database variable name, which is used in returning the data store content to callers
	 * @return An {@link Optional} containing the database variable name
	 */
	public Optional<String> getDatabaseVariableName() {
		return databaseVariableName;
	}
	
	/**
	 * Returns the list of stored characteristic values, which are propagated to the caller
	 * @return Return the List of all stored characteristic values
	 */
	public List<CharacteristicValue> getCharacteristicValues() {
		return characteristicValues;
	}
}
