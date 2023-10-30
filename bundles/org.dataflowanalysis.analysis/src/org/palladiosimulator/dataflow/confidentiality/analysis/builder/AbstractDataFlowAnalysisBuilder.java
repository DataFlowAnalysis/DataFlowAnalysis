package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;

/**
 * An abstract builder for a data flow analysis.
 * Implementation of the builder are responsible for the following
 * - Creation of the Analysis
 * - Validating the builder data
 * - Allowing creation of the builder with expected builder data
 *
 * @param <T> Type of the Analysis that is returned by the builder
 * @param <D> Type of the Data that is saved in the builder
 * @param <E> Expected Data from which the builder can be created
 */
public abstract class AbstractDataFlowAnalysisBuilder
<T extends DataFlowConfidentialityAnalysis, 
D extends AnalysisBuilderData, 
E extends AnalysisBuilderData> {
	protected final Logger logger = Logger.getLogger(DataFlowAnalysisBuilder.class);
	protected final D builderData;
	protected final List<AbstractDataFlowAnalysisBuilder<?, ?, ?>> builder;

	/**
	 * Creates a new builder with the given builder data
	 * @param builderData Initial builder data
	 */
	public AbstractDataFlowAnalysisBuilder(D builderData) {
		this.builderData = builderData;
		this.builder = new ArrayList<>();
	}
	
	/**
	 * Copies the given expected builder data into the builder
	 * @param builderData Expected builder data
	 */
	public abstract void copyBuilderData(E builderData);
	
	/**
	 * Check the builder data that is saved in the builder
	 * <p>
	 * Checking the builder data is left to the implementation.
	 * It may call {@code checkBuilderData()} of previous builders
	 */
	public abstract void validateBuilderData();
	
	/**
	 * Builds the analysis with the given builder data
	 * @return Returns new analysis with the given data
	 */
	public abstract T build();
	
	/**
	 * Uses the given builder to build a subtype of the data flow analysis
	 * @param <B> Builder class that is used
	 * @param builder Instance of the builder object that should be used
	 * @return Returns the builder object of the given builder
	 */
	public <B extends AbstractDataFlowAnalysisBuilder<?, ?, D>> B useBuilder(B builder) {
		this.builder.add(builder);
		builder.copyBuilderData(builderData);
		return builder;
	}
	
	/**
	 * Returns the saved builder data of the builder
	 * @return Saved builder data
	 */
	public D getBuilderData() {
		return this.builderData;
	}
}
