package org.dataflowanalysis.analysis.dfd;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraph;
import org.dataflowanalysis.analysis.dfd.resource.DFDResourceProvider;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.flowgraph.FlowGraph;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;

public class DFDConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
  private final Logger logger = Logger.getLogger(DFDConfidentialityAnalysis.class);

  protected DFDResourceProvider resourceProvider;
  protected Optional<Class<? extends Plugin>> modelProjectActivator;
  protected String modelProjectName;

  public DFDConfidentialityAnalysis(
      DFDResourceProvider resourceProvider,
      Optional<Class<? extends Plugin>> modelProjectActivator,
      String modelProjectName) {
    this.resourceProvider = resourceProvider;
    this.modelProjectActivator = modelProjectActivator;
    this.modelProjectName = modelProjectName;
  }

  @Override
  public boolean initializeAnalysis() {
    Resource.Factory.Registry.INSTANCE
        .getExtensionToFactoryMap()
        .put("dataflowdiagram", new XMIResourceFactoryImpl());
    Resource.Factory.Registry.INSTANCE
        .getExtensionToFactoryMap()
        .put("datadictionary", new XMIResourceFactoryImpl());

    EcorePlugin.ExtensionProcessor.process(null);

    try {
      var initializationBuilder =
          StandaloneInitializerBuilder.builder()
              .registerProjectURI(
                  DFDConfidentialityAnalysis.class, DFDConfidentialityAnalysis.PLUGIN_PATH);

      if (this.modelProjectActivator.isPresent()) {
        initializationBuilder.registerProjectURI(
            this.modelProjectActivator.get(), this.modelProjectName);
      }

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
    return true;
  }

  @Override
  public DFDFlowGraph findFlowGraph() {
    return new DFDFlowGraph(this.resourceProvider);
  }

  @Override
  public DFDFlowGraph evaluateFlowGraph(FlowGraph flowGraph) {
    if (!(flowGraph instanceof DFDFlowGraph)) {
      logger.error("Cannot evaluate a non-dfd flow graph!", new IllegalArgumentException());
    }
    DFDFlowGraph dfdFlowGraph = (DFDFlowGraph) flowGraph;
    return dfdFlowGraph.evaluate();
  }

  @Override
  public List<? extends AbstractVertex<?>> queryDataFlow(
      AbstractPartialFlowGraph sequence, Predicate<? super AbstractVertex<?>> condition) {
    return sequence.getVertices().parallelStream().filter(condition).toList();
  }

  @Override
  public void setLoggerLevel(Level level) {
    logger.setLevel(level);
  }
}
