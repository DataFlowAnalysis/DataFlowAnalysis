package org.dataflowanalysis.analysis.builder.dfd;

import java.util.Optional;

import org.dataflowanalysis.analysis.DFDConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.DataFlowAnalysisBuilder;
import org.eclipse.core.runtime.Plugin;

public class DFDDataFlowAnalysisBuilder extends DataFlowAnalysisBuilder {
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

	public DFDConfidentialityAnalysis build() {
		this.builderData.validateData();
		return new DFDConfidentialityAnalysis(this.builderData.createAnalysisData(), this.builderData.getPluginActivator(), this.builderData.getModelProjectName());
	}
}
