package org.dataflowanalysis.analysis.builder.validation;

import org.apache.log4j.Logger;

/**
 * This class represents a validation error that occurred during the creation of the analysis
 * and is returned by {@link ValidationObject#validate()}}
 *
 */
public class ValidationError {
	private String message;
	private ValidationErrorSeverity severity;
	
	/**
	 * Instantiates a new validation error from the given message with a given severity
	 * @param message Message of the validation error
	 * @param severity Severity of the validation error
	 */
	public ValidationError(String message, ValidationErrorSeverity severity) {
		this.message = message;
		this.severity = severity;
	}
	
	/**
	 * Logs a validation error to the given logger
	 * @param logger Logger that should be used to log the occurred validation error
	 */
	public void log(Logger logger) {
		switch (this.severity) {
			case ERROR -> logger.error(this.message);
			case WARNING -> logger.warn(this.message);
			case INFO -> logger.warn(this.message);
		}
	}
	
	/**
	 * Indicates whether a validation error is fatal. Currently only validation errors are fatal
	 * @return 	Returns true, if the validation error is fatal. 
	 * 			Otherwise, this method returns false
	 */
	public boolean isFatal() {
		return this.severity == ValidationErrorSeverity.ERROR;
		
	}
}
