package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics;

import mdpa.dfd.datadictionary.Label;
import mdpa.dfd.datadictionary.LabelType;

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
