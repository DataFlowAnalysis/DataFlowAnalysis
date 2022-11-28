package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;

public class DataStore {
	private String databaseComponentName;
	private Optional<String> databaseVariableName;
	private List<CharacteristicValue> characteristicValues;

	public DataStore(String databaseComponentName) {
		this.databaseComponentName = databaseComponentName;
		this.databaseVariableName = Optional.empty();
		this.characteristicValues = new ArrayList<>();
	}
	
	public void setCharacteristicValues(List<CharacteristicValue> characteristicValues) {
		this.characteristicValues = characteristicValues;
	}
	
	public String getDatabaseComponentName() {
		return databaseComponentName;
	}
	
	public void setDatabaseVariableName(String databaseVariableName) {
		this.databaseVariableName = Optional.of(databaseVariableName);
	}
	
	public Optional<String> getDatabaseVariableName() {
		return databaseVariableName;
	}
	
	public List<CharacteristicValue> getCharacteristicValues() {
		return characteristicValues;
	}
}
