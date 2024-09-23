package org.dataflowanalysis.analysis;

import java.util.Optional;
import org.apache.log4j.*;
import org.eclipse.core.runtime.Plugin;

/**
 * This class represents a basic builder for a {@link DataFlowConfidentialityAnalysis}. A concrete subclass of the
 * builder must implement the {@link DataFlowAnalysisBuilder#build()} method and return a valid
 * {@link DataFlowConfidentialityAnalysis} or one of its subclasses. The data contained in this builder can be accessed
 * in subclasses and validated via {@link DataFlowAnalysisBuilder#validate()}.
 */
public abstract class DataFlowAnalysisBuilder {
    private final Logger logger = Logger.getLogger(DataFlowAnalysisBuilder.class);

    protected boolean standalone;
    protected String modelProjectName;
    protected Optional<Class<? extends Plugin>> pluginActivator;
    protected boolean customResourceProviderIsLoaded = false;

    /**
     * Create a new {@link DataFlowAnalysisBuilder}
     */
    public DataFlowAnalysisBuilder() {
        this.pluginActivator = Optional.empty();
        this.modelProjectName = "";
        this.standalone = false;
    }

    /**
     * Sets standalone mode of the analysis. Currently, this method must be called to build a valid analysis
     * @return Builder of the analysis
     */
    public DataFlowAnalysisBuilder standalone() {
        this.standalone = true;
        return this;
    }

    /**
     * Sets the model project name of the analysis that is used to resolve paths to the files of the model. Example: For
     * models contained in the {@code org.dataflowanalysis.analysis.testmodels} project/bundle the modelProjectName would be equal
     * to that name.
     * @return Builder of the analysis
     */
    public DataFlowAnalysisBuilder modelProjectName(String modelProjectName) {
        this.modelProjectName = modelProjectName;
        return this;
    }

    /**
     * Sets the plugin activator project name of the analysis. The plugin activator is required to load model files from a
     * project outside the analysis project. Example: For the models contained in the
     * {@code org.dataflowanalysis.analysis.testmodels} project/bundle the pluginActivator is the basic class present in the
     * sources of that project
     * @return Builder of the analysis
     */
    public DataFlowAnalysisBuilder usePluginActivator(Class<? extends Plugin> activator) {
        this.pluginActivator = Optional.of(activator);
        return this;
    }

    /**
     * Validates the stored data and finds potential issues that prevent the analysis from working correctly.
     * @throws IllegalStateException This method throws an {@link IllegalStateException} when an analysis cannot be built
     * with the current data
     */
    protected void validate() {
        if (!this.standalone) {
            logger.error("The dataflow analysis can only be run in standalone mode",
                    new IllegalStateException("Dataflow analysis can only be run in standalone mode"));
        }
        if (!customResourceProviderIsLoaded && (this.modelProjectName == null || this.modelProjectName.isEmpty())) {
            logger.error("The dataflow analysis requires a model project name to be present to resolve paths to" + " the models",
                    new IllegalStateException("Model project name is required"));
        }
    }

    /**
     * Builds a new analysis from the given data and returns the analysis object.
     * @throws IllegalStateException This method throws an {@link IllegalStateException} when an analysis cannot be built
     * with the current data
     */
    public abstract DataFlowConfidentialityAnalysis build();
}
