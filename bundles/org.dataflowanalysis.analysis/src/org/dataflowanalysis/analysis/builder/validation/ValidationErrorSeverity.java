package org.dataflowanalysis.analysis.builder.validation;

/**
 * This enum represents the different level of validation errors that can occur during validation
 *
 */
public enum ValidationErrorSeverity {
	/**
	 * A validation error so severe the analysis cannot be created
	 */
	ERROR, 
	/**
	 * A validation error, which might break the analysis or produce unexpected results
	 */
	WARNING, 
	/**
	 * A validation error that is purely informational
	 */
	INFO
}
