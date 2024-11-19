package org.dataflowanalysis.analysis.dfd;

import java.nio.file.Paths;
import java.util.Optional;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowAnalysisBuilder;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.resource.DFDModelResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.dfd.resource.DFDURIResourceProvider;
import org.dataflowanalysis.analysis.utils.ResourceUtils;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;

/**
 * This class is used to build an instance of {@link DFDConfidentialityAnalysis}. The data contained in this class is
 * validated, when calling {@link DFDDataFlowAnalysisBuilder#build()} before an analysis object is returned
 */
public class DFDDataFlowAnalysisBuilder extends DataFlowAnalysisBuilder {
    private final Logger logger = Logger.getLogger(DFDDataFlowAnalysisBuilder.class);

    protected String dataFlowDiagramPath;
    protected String dataDictionaryPath;
    protected Optional<DFDResourceProvider> customResourceProvider;
    protected Class<? extends TransposeFlowGraphFinder> customTransposeFlowGraphFinderClass;

    /**
     * Constructs a dfd analysis builder with empty values
     */
    public DFDDataFlowAnalysisBuilder() {
        this.customResourceProvider = Optional.empty();
        this.customTransposeFlowGraphFinderClass = null;
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
        if (resourceProvider instanceof DFDModelResourceProvider) customResourceProviderIsLoaded = true;
        return this;
    }

    /**
     * Registers a custom TransposeFlowGraphFinder for the analysis
     * @param transposeFlowGraphFinder Custom TransposeFlowGraphFinder of the analysis
     */
    public DFDDataFlowAnalysisBuilder useTransposeFlowGraphFinder(Class<? extends TransposeFlowGraphFinder> transposeFlowGraphFinderClass) {
        this.customTransposeFlowGraphFinderClass = transposeFlowGraphFinderClass;
        return this;
    }

    /**
     * Determines the effective resource provider that should be used by the analysis
     */
    private DFDResourceProvider getEffectiveResourceProvider() {
    	URI dataDictionaryUri = Paths.get(this.dataDictionaryPath).isAbsolute() ? URI.createFileURI(this.dataDictionaryPath) 
    			:  ResourceUtils.createRelativePluginURI(this.dataDictionaryPath, this.modelProjectName);
    	URI dataFlowDiagramUri = Paths.get(this.dataFlowDiagramPath).isAbsolute() ? URI.createFileURI(this.dataFlowDiagramPath) 
    			:  ResourceUtils.createRelativePluginURI(this.dataFlowDiagramPath, this.modelProjectName);
    	
        if (this.customResourceProvider.isEmpty()) {
            return new DFDURIResourceProvider(dataFlowDiagramUri, dataDictionaryUri);
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
        DFDResourceProvider resourceProvider = this.getEffectiveResourceProvider();

        if (customTransposeFlowGraphFinderClass == null)
            return new DFDConfidentialityAnalysis(resourceProvider, this.pluginActivator, this.modelProjectName);
        else
            return new DFDConfidentialityAnalysis(resourceProvider, this.pluginActivator, this.modelProjectName,
                    this.customTransposeFlowGraphFinderClass);
    }
}
