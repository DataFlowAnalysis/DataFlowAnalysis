package org.dataflowanalysis.analysis.dfd;

import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.eclipse.core.runtime.Plugin;

/**
 * This class is used to build an instance of {@link DFDConfidentialityAnalysis}. The data contained in this class is
 * validated, when calling {@link DFDDataFlowAnalysisBuilder#build()} before an analysis object is returned
 */
public class DFDDataFlowAnalysisBuilder extends DataFlowAnalysisBuilder {
    private final Logger logger = Logger.getLogger(DFDDataFlowAnalysisBuilder.class);

    protected String dataFlowDiagramPath;
    protected String dataDictionaryPath;
    protected Optional<DFDResourceProvider> customResourceProvider;
    protected Class<? extends TransposeFlowGraphFinder > customTransposeFlowGraphFinder;

    /**
     * Constructs a dfd analysis builder with empty values
     */
    public DFDDataFlowAnalysisBuilder() {
        this.customResourceProvider = Optional.empty();
        this.customTransposeFlowGraphFinder = null;
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
    public DFDDataFlowAnalysisBuilder useCustomResourceProvider(DFDResourceProvider resourceProvider) {
        this.customResourceProvider = Optional.of(resourceProvider);
        return this;
    }
    
    /**
     * Registers a custom TransposeFlowGraphFinder for the analysis
     * @param transposeFlowGraphFinder Custom TransposeFlowGraphFinder of the analysis
     */
    public DFDDataFlowAnalysisBuilder useTransposeFlowGraphFinder(Class<? extends TransposeFlowGraphFinder> transposeFlowGraphFinder) {
        this.customTransposeFlowGraphFinder = transposeFlowGraphFinder;
        return this;
    }

    /**
     * Determines the effective resource provider that should be used by the analysis
     */
    private DFDResourceProvider getEffectiveResourceProvider() {
    	if (this.customResourceProvider.isEmpty()) {
    		return new DFDURIResourceProvider(ResourceUtils.createRelativePluginURI(this.dataFlowDiagramPath, this.modelProjectName),
                    ResourceUtils.createRelativePluginURI(this.dataDictionaryPath, this.modelProjectName));
    	}
    	return this.customResourceProvider.get();
    }
    

    /**
     * Validates the stored data
     */
    protected void validate() {
        super.validate();
        if (this.customResourceProvider.isEmpty() && (this.dataDictionaryPath == null || this.dataDictionaryPath.isEmpty())) {
            logger.error("A data dictionary is required to run the data flow analysis",
                    new IllegalStateException("The DFD analysis requires a data dictionary"));
        }
        if (this.customResourceProvider.isEmpty() && (this.dataFlowDiagramPath == null || this.dataFlowDiagramPath.isEmpty())) {
            logger.error("A data flow diagram is required to run the data flow analysis",
                    new IllegalStateException("The DFD analysis requires a data flow diagram"));
        }
    }

    /**
     * Builds a new analysis from the given data
     */
    public DFDConfidentialityAnalysis build() {
        this.validate();
        DFDResourceProvider ressourceProvider = this.getEffectiveResourceProvider();
        
        if(customTransposeFlowGraphFinder == null) 
            return new DFDConfidentialityAnalysis(ressourceProvider,this.pluginActivator, this.modelProjectName);            
        else
            return new DFDConfidentialityAnalysis(ressourceProvider, this.pluginActivator, this.modelProjectName, this.customTransposeFlowGraphFinder);
    }
}
