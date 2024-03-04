package org.dataflowanalysis.analysis.dfd;

import java.util.Optional;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.EnhancedPatternLayout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraph;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
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

  public DFDConfidentialityAnalysis(
      DFDResourceProvider resourceProvider,
      Optional<Class<? extends Plugin>> modelProjectActivator,
      String modelProjectName) {
    this.resourceProvider = resourceProvider;
    this.modelProjectActivator = modelProjectActivator;
    this.modelProjectName = modelProjectName;
  }

    @Override
    public void initializeAnalysis() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("dataflowdiagram", new XMIResourceFactoryImpl());
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("datadictionary", new XMIResourceFactoryImpl());

        EcorePlugin.ExtensionProcessor.process(null);

        try {
        	BasicConfigurator.resetConfiguration();
          BasicConfigurator.configure(
              new ConsoleAppender(new EnhancedPatternLayout("%-6r [%p] %-35C{1} - %m%n")));

          Logger.getLogger(AbstractInternalAntlrParser.class).setLevel(Level.WARN);
          Logger.getLogger(DefaultLinkingService.class).setLevel(Level.WARN);
          Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class).setLevel(Level.WARN);
          Logger.getLogger(AbstractCleaningLinker.class).setLevel(Level.WARN);

          logger.info("Successfully initialized standalone log4j for the data flow analysis.");
            var initializationBuilder = StandaloneInitializerBuilder.builder().registerProjectURI(DFDConfidentialityAnalysis.class,
                    DFDConfidentialityAnalysis.PLUGIN_PATH);

            this.modelProjectActivator
                    .ifPresent(projectActivator -> initializationBuilder.registerProjectURI(projectActivator, this.modelProjectName));

            initializationBuilder.build().init();

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
    public DFDFlowGraph findFlowGraph() {
        return new DFDFlowGraph(this.resourceProvider);
    }

    @Override
    public void setLoggerLevel(Level level) {
        logger.setLevel(level);
    }
}
