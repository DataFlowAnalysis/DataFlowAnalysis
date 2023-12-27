package org.dataflowanalysis.analysis.dfd.builder;

import java.util.ArrayList;
import java.util.List;

import org.dataflowanalysis.analysis.AnalysisData;
import org.dataflowanalysis.analysis.builder.AnalysisBuilderData;
import org.dataflowanalysis.analysis.builder.validation.ValidationError;
import org.dataflowanalysis.analysis.builder.validation.ValidationErrorSeverity;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;

public class DFDAnalysisBuilderData extends AnalysisBuilderData {
	private String dataFlowDiagramPath;
	private String dataDictionaryPath;
	
	public DFDAnalysisBuilderData() {}
	
	public List<ValidationError> validate() {
		List<ValidationError> validationErrors = new ArrayList<>(super.validate());
		if (this.dataDictionaryPath == null || this.dataDictionaryPath.isBlank()) {
			validationErrors.add(new ValidationError("A path to a data dictionary is required", ValidationErrorSeverity.ERROR));
		}
		if (this.dataFlowDiagramPath == null || this.dataFlowDiagramPath.isBlank()) {
			validationErrors.add(new ValidationError("A path to a data flow diagram is required", ValidationErrorSeverity.ERROR));
		}
		return validationErrors;
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
