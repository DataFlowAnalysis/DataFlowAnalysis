package org.dataflowanalysis.analysis.dfd.builder;

import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;

public class DFDAnalysisBuilderData extends AnalysisBuilderData {
	private String dataFlowDiagramPath;
	private String dataDictionaryPath;
	
	public DFDAnalysisBuilderData() {}
	
	public void setDataFlowDiagramPath(String dataFlowDiagramPath) {
		this.dataFlowDiagramPath = dataFlowDiagramPath;
	}
	
	public void setDataDictionaryPath(String dataDictionaryPath) {
		this.dataDictionaryPath = dataDictionaryPath;
	}
	public AnalysisData createAnalysisData() {
		DFDResourceProvider resourceProvider = this.getEffectiveResourceProvider();
		return new AnalysisData(resourceProvider, null, null);
	}

	private DFDResourceProvider getEffectiveResourceProvider() {
		return new DFDURIResourceProvider(ResourceUtils.createRelativePluginURI(this.dataFlowDiagramPath, this.modelProjectName), ResourceUtils.createRelativePluginURI(this.dataDictionaryPath, this.modelProjectName));
	}
}
