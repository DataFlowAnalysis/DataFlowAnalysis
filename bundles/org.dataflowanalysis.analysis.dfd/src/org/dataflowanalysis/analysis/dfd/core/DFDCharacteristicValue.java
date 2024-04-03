package org.dataflowanalysis.analysis.dfd.core;

import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;

/**
 * This class represents a characteristic value in a dfd model
 * @param labelType Label type model object of the characteristic value
 * @param label Label model object of the characteristic value
 */
public record DFDCharacteristicValue(LabelType labelType, Label label) implements CharacteristicValue {

    @Override
    public String getTypeName() {
        return this.labelType()
                .getEntityName();
    }

    @Override
    public String getValueName() {
        return this.label()
                .getEntityName();
    }

    @Override
    public String getValueId() {
        return this.label()
                .getId();
    }

    /**
     * Returns the label stored in the Characteristics Value
     * @return Label
     */
    public Label getLabel() {
        return this.label;
    }
}
