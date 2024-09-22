package org.dataflowanalysis.analysis.dfd;

import java.util.Optional;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.TransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

/**
 * This class represents a toplevel dfd confidentiality analysis which allows analysis of a given model
 */
public class DFDConfidentialityAnalysis extends DataFlowConfidentialityAnalysis {
    private final Logger logger = Logger.getLogger(DFDConfidentialityAnalysis.class);

    protected final DFDResourceProvider resourceProvider;
    protected final Optional<Class<? extends Plugin>> modelProjectActivator;
    protected final String modelProjectName;
    protected final Class<? extends TransposeFlowGraphFinder> transposeFlowGraphFinderClass;

    public DFDConfidentialityAnalysis(DFDResourceProvider resourceProvider, Optional<Class<? extends Plugin>> modelProjectActivator,
            String modelProjectName, Class<? extends TransposeFlowGraphFinder> transposeFlowGraphFinderClass) {
        this.resourceProvider = resourceProvider;
        this.modelProjectActivator = modelProjectActivator;
        this.modelProjectName = modelProjectName;
        this.transposeFlowGraphFinderClass = transposeFlowGraphFinderClass;
    }

    public DFDConfidentialityAnalysis(DFDResourceProvider resourceProvider, Optional<Class<? extends Plugin>> modelProjectActivator,
            String modelProjectName) {
        this.resourceProvider = resourceProvider;
        this.modelProjectActivator = modelProjectActivator;
        this.modelProjectName = modelProjectName;
        this.transposeFlowGraphFinderClass = DFDTransposeFlowGraphFinder.class;
    }

    @Override
    public void initializeAnalysis() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("dataflowdiagram", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap()
                .put("datadictionary", new XMIResourceFactoryImpl());

        EcorePlugin.ExtensionProcessor.process(null);

        try {
            super.setupLoggers();
            var initializationBuilder = StandaloneInitializerBuilder.builder()
                    .registerProjectURI(DFDConfidentialityAnalysis.class, DFDConfidentialityAnalysis.PLUGIN_PATH);

            this.modelProjectActivator
                    .ifPresent(projectActivator -> initializationBuilder.registerProjectURI(projectActivator, this.modelProjectName));

            initializationBuilder.build()
                    .init();

            logger.info("Successfully initialized standalone environment for the data flow analysis.");

        } catch (StandaloneInitializationException e) {
            logger.error("Could not initialize analysis", e);
            throw new IllegalStateException("Could not initialize analysis");
        }
        this.resourceProvider.loadRequiredResources();
        if (!this.resourceProvider.sufficientResourcesLoaded()) {
            logger.error("Insufficient amount of resources loaded");
            throw new IllegalStateException("Could not initialize analysis");
        }
    }

    @Override
    public DFDFlowGraphCollection findFlowGraphs() {

        return new DFDFlowGraphCollection(this.resourceProvider, this.transposeFlowGraphFinderClass);
    }

    @Override
    public void setLoggerLevel(Level level) {
        logger.setLevel(level);
    }
}
