package org.palladiosimulator.dataflow.confidentiality.analysis.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;

public abstract class AbstractDataFlowAnalysisBuilder<T extends DataFlowConfidentialityAnalysis, D extends AnalysisBuilderData> {
	protected final Logger logger = Logger.getLogger(DataFlowConfidentialityAnalysisBuilder.class);
	protected final D builderData;
	protected final List<AbstractDataFlowAnalysisBuilder<?, ?>> builder;

	/**
	 * Creates a new builder with the given builder data
	 * @param builderData Initial builder data
	 */
	public AbstractDataFlowAnalysisBuilder(D builderData) {
		this.builderData = builderData;
		this.builder = new ArrayList<>();
	}
	
	public abstract void copyBuilderData(AnalysisBuilderData builderData);
	
	/**
	 * Check the builder data that is saved in the builder
	 * <p>
	 * Checking the builder data is left to the implementation.
	 * It may call {@code checkBuilderData()} of previous builders
	 */
	public abstract void checkBuilderData();
	
	/**
	 * Builds the analysis with the given builder data
	 * @return Returns new analysis with the given data
	 */
	public abstract T build();
	
	public <B extends AbstractDataFlowAnalysisBuilder<?, ?>> B registerBuilder(B builder) {
		this.builder.add(builder);
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
