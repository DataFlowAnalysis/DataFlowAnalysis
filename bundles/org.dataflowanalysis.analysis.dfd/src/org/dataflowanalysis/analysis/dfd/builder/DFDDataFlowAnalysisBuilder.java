package org.dataflowanalysis.analysis.dfd.builder;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.builder.validation.ValidationError;
import org.dataflowanalysis.analysis.dfd.DFDConfidentialityAnalysis;
import org.eclipse.core.runtime.Plugin;

public class DFDDataFlowAnalysisBuilder extends DataFlowAnalysisBuilder {
	private final Logger logger = Logger.getLogger(DFDDataFlowAnalysisBuilder.class);
	
	private DFDAnalysisBuilderData builderData;

	public DFDDataFlowAnalysisBuilder() {
		this.builderData = new DFDAnalysisBuilderData();
	}	
	
	/**
	 * Sets standalone mode of the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder standalone() {
		this.builderData.setStandalone(true);
		return this;
	}
	
	/**
	 * Sets the modelling project name of the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
		this.builderData.setModelProjectName(modelProjectName);
		return this;
	}
	
	/**
	 * Uses a plugin activator class for the given project
	 * @param pluginActivator Plugin activator class of the modeling project
	 * @return Returns builder object of the analysis
	 */
	public DFDDataFlowAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
		this.builderData.setPluginActivator(Optional.of(pluginActivator));
		return this;
	}
	
	/**
	 * Sets the data dictionary used by the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder useDataDictionary(String dataDictionaryPath) {
		this.builderData.setDataDictionaryPath(dataDictionaryPath);
		return this;
	}
	
	/**
	 * Sets the data dictionary used by the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder useDataFlowDiagram(String dataFlowDiagramPath) {
		this.builderData.setDataFlowDiagramPath(dataFlowDiagramPath);
		return this;
	}

	/**
	 * Builds a new analysis from the given data
	 */
	public DFDConfidentialityAnalysis build() {
		List<ValidationError> validationErrors = this.builderData.validate();
		validationErrors.forEach(it -> it.log(logger));
		if (validationErrors.stream().anyMatch(ValidationError::isFatal)) {
			logger.fatal("Could not create analysis due to fatal validation errors");
			throw new IllegalStateException();
		}
		return new DFDConfidentialityAnalysis(this.builderData.createAnalysisData(), this.builderData.getPluginActivator(), this.builderData.getModelProjectName());
	}
}
