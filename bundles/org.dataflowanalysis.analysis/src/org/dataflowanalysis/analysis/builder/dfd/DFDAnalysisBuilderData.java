package org.dataflowanalysis.analysis.builder.dfd;

import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.resource.dfd.DFDResourceProvider;
import org.dataflowanalysis.analysis.resource.dfd.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.pcm.ResourceUtils;

public class DFDAnalysisBuilderData extends AnalysisBuilderData {
	private String dataFlowDiagramPath;
	private String dataDictionaryPath;
	
	public DFDAnalysisBuilderData() {}
	
	public void validateData() {
		if (this.dataDictionaryPath == null || this.dataDictionaryPath.isBlank()) {
			throw new IllegalStateException("A path to a data dictionary is required");
		}
		if (this.dataFlowDiagramPath == null || this.dataFlowDiagramPath.isBlank()) {
			throw new IllegalStateException("A path to a data flow diagram is required");
		}
	}
	
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
