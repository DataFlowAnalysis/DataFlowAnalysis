package org.dataflowanalysis.analysis.dfd;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.eclipse.core.runtime.Plugin;

public class DFDDataFlowAnalysisBuilder extends DataFlowAnalysisBuilder {
	private final Logger logger = Logger.getLogger(DFDDataFlowAnalysisBuilder.class);

	private String dataFlowDiagramPath;
	private String dataDictionaryPath;
	private Optional<DFDResourceProvider> customResourceProvider;

	public DFDDataFlowAnalysisBuilder() {
	}	
	
	/**
	 * Sets standalone mode of the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder standalone() {
		super.standalone();
		return this;
	}
	
	/**
	 * Sets the modelling project name of the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
		super.modelProjectName(modelProjectName);
		return this;
	}
	
	/**
	 * Uses a plugin activator class for the given project
	 * @param pluginActivator Plugin activator class of the modeling project
	 * @return Returns builder object of the analysis
	 */
	public DFDDataFlowAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
		super.usePluginActivator(pluginActivator);
		return this;
	}
	
	/**
	 * Sets the data dictionary used by the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder useDataDictionary(String dataDictionaryPath) {
		this.dataDictionaryPath = dataDictionaryPath;
		return this;
	}
	
	/**
	 * Sets the data dictionary used by the analysis
	 * @return Builder of the analysis
	 */
	public DFDDataFlowAnalysisBuilder useDataFlowDiagram(String dataFlowDiagramPath) {
		this.dataFlowDiagramPath = dataFlowDiagramPath;
		return this;
	}
	
	/**
	 * Registers a custom resource provider for the analysis
	 * @param resourceProvider Custom resource provider of the analysis
	 */
	public void useCustomResourceProvider(DFDResourceProvider resourceProvider) {
		this.customResourceProvider = Optional.of(resourceProvider);
	}
	
	/**
	 * Create analysis // TODO Remove
	 * @return
	 */
	private AnalysisData createAnalysisData() {
		DFDResourceProvider resourceProvider = this.getEffectiveResourceProvider();
		return new AnalysisData(resourceProvider, null, null);
	}

	/**
	 * Determines the effective resource provider that should be used by the analysis
	 */
	private DFDResourceProvider getEffectiveResourceProvider() {
		return this.customResourceProvider
				.orElse(new DFDURIResourceProvider(ResourceUtils.createRelativePluginURI(this.dataFlowDiagramPath, this.modelProjectName), ResourceUtils.createRelativePluginURI(this.dataDictionaryPath, this.modelProjectName)));
	}
	
	/**
	 * Validates the stored data
	 */
	protected void validate() {
		super.validate();
		if (this.dataDictionaryPath == null || this.dataDictionaryPath.isEmpty()) {
			logger.error("A data dictionary is required to run the data flow analysis", new IllegalStateException("The DFD analysis requires a data dictionary"));
		}
		if (this.dataFlowDiagramPath == null || this.dataFlowDiagramPath.isEmpty()) {
			logger.error("A data flow diagram is required to run the data flow analysis", new IllegalStateException("The DFD analysis requires a data flow diagram"));
		}
	}

	/**
	 * Builds a new analysis from the given data
	 */
	public DFDConfidentialityAnalysis build() {
		this.validate();
		return new DFDConfidentialityAnalysis(this.createAnalysisData(), this.pluginActivator, this.modelProjectName);
	}
}
