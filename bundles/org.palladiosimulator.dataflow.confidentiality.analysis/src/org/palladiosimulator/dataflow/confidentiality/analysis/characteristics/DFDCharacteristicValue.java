package org.palladiosimulator.dataflow.confidentiality.analysis.characteristics;

import mdpa.dfd.datadictionary.Label;
import mdpa.dfd.datadictionary.LabelType;

public record DFDCharacteristicValue(LabelType labelType, Label label) implements CharacteristicValue {

}
