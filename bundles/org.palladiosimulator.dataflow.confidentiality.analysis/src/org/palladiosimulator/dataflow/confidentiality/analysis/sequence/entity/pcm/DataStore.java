package org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.CharacteristicValue;

public class DataStore {
	private String databaseComponentName;
	private List<CharacteristicValue> characteristicValues;

	public DataStore(String databaseComponentName) {
		this.databaseComponentName = databaseComponentName;
		this.characteristicValues = new ArrayList<>();
	}
	
	public void setCharacteristicValues(List<CharacteristicValue> characteristicValues) {
		this.characteristicValues = characteristicValues;
	}
	
	public String getDatabaseComponentName() {
		return databaseComponentName;
	}
	
	public List<CharacteristicValue> getCharacteristicValues() {
		return characteristicValues;
	}
}
