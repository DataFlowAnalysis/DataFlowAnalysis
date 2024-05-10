package org.dataflowanalysis.analysis.pcm.informationflow;

import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategy;
import org.dataflowanalysis.analysis.pcm.informationflow.core.extraction.IFPCMExtractionStrategyPreferConsider;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.eclipse.core.runtime.Plugin;

/**
 * A builder for an {@link IFPCMDataFlowConfidentialityAnalysis}.
 */
public class IFPCMDataFlowConfidentialityAnalysisBuilder extends PCMDataFlowConfidentialityAnalysisBuilder {

    private boolean considerImplictFlows;
    private IFPCMExtractionStrategy extractionStrategy;

    public IFPCMDataFlowConfidentialityAnalysisBuilder() {
        super();
        considerImplictFlows = false;
        extractionStrategy = new IFPCMExtractionStrategyPreferConsider("Lattice");
    }

    /**
     * Sets whether the analysis should consider implicit flows.
     * @param consider true, if the analysis should consider implicit flows. false, otherwise
     * @return the builder object of the analysis
     */
    public IFPCMDataFlowConfidentialityAnalysisBuilder setConsiderImplicitFlow(boolean consider) {
        this.considerImplictFlows = consider;
        return this;
    }

    /**
     * Sets the used extractionStrategy for the analysis.
     * @param extractionStrategy the extractionStrategy
     * @return the builder object of the analysis
     */
    public IFPCMDataFlowConfidentialityAnalysisBuilder setExtractionStrategy(IFPCMExtractionStrategy extractionStrategy) {
        this.extractionStrategy = extractionStrategy;
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysis build() {
        var analysis = super.build();
        extractionStrategy.initialize(analysis.getResourceProvider());

        return new IFPCMDataFlowConfidentialityAnalysis(analysis.getResourceProvider(), modelProjectName, pluginActivator, considerImplictFlows,
                extractionStrategy);
    }

    /*
     * From here on only changed signatures.
     */

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder usePluginActivator(Class<? extends Plugin> pluginActivator) {
        super.usePluginActivator(pluginActivator);
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder useUsageModel(String relativeUsageModelPath) {
        super.useUsageModel(relativeUsageModelPath);
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder useAllocationModel(String relativeAllocationModelPath) {
        super.useAllocationModel(relativeAllocationModelPath);
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder useNodeCharacteristicsModel(String relativeNodeCharacteristicsModelPath) {
        super.useNodeCharacteristicsModel(relativeNodeCharacteristicsModelPath);
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder useCustomResourceProvider(PCMResourceProvider resourceProvider) {
        super.useCustomResourceProvider(resourceProvider);
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder standalone() {
        super.standalone();
        return this;
    }

    @Override
    public IFPCMDataFlowConfidentialityAnalysisBuilder modelProjectName(String modelProjectName) {
        super.modelProjectName(modelProjectName);
        return this;
    }

}
