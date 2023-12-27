package org.dataflowanalysis.analysis.builder.validation;

import java.util.List;

/**
 * This interface may be implemented by any component that can be validated
 *
 */
public interface ValidationObject {
	/**
	 * Validates the data stored in the implementing object
	 * @return Returns a list of all validation errors that occurred
	 */
	List<ValidationError> validate();
}
