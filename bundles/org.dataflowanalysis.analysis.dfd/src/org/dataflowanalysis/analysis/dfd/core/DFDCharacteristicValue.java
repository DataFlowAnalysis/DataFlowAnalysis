package org.dataflowanalysis.analysis.dfd.core;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;

public record DFDCharacteristicValue(LabelType labelType, Label label) implements CharacteristicValue {

	@Override
	public String getTypeName() {
		return this.labelType().getEntityName();
	}

	@Override
	public String getValueName() {
		return this.label().getEntityName();
	}

	@Override
	public String getValueId() {
		return this.label().getId();
	}

}