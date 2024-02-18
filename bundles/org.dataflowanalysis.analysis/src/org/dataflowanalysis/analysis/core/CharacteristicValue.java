package org.dataflowanalysis.analysis.core;

/**
 * This interface represents an element that represents a characteristic value with a given characteristic type name and characteristic value name.
 */
public interface CharacteristicValue {
    /**
     * This method returns the name of the characteristic type
     * @return Returns the name of the characteristic type
     */
    String getTypeName();

    /**
     * This method returns the name of the characteristic value
     * @return Returns the name of the characteristic value
     */
    String getValueName();

    /**
     * This method returns the identifier of the characteristic value in the model
     * @return Returns the identifier of the characteristic value
     */
    String getValueId();
}
