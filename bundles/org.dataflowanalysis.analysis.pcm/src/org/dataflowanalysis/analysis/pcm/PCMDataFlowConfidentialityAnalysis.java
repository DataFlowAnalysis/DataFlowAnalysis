package org.dataflowanalysis.analysis.pcm;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.FlowGraph;
import org.dataflowanalysis.analysis.pcm.core.PCMFlowGraph;
import org.dataflowanalysis.analysis.pcm.core.PCMVertexCharacteristicsCalculator;
import org.dataflowanalysis.analysis.pcm.resource.PCMResourceProvider;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.pcm.extension.dddsl.DDDslStandaloneSetup;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class PCMDataFlowConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
    private static final String PLUGIN_PATH = "org.dataflowanalysis.analysis.pcm";
    private final Logger logger;

    protected final PCMResourceProvider resourceProvider;

    protected final String modelProjectName;
    protected final Optional<Class<? extends Plugin>> modelProjectActivator;

    protected List<PCMDataDictionary> dataDictionaries;

    /**
     * Creates a new instance of an data flow analysis with the given parameters
     * @param resourceProvider Resource provider that provides the required resources to the analysis
     * @param modelProjectName Name of the modelling project
     * @param modelProjectActivator Plugin class of the analysis
     */
    public PCMDataFlowConfidentialityAnalysis(PCMResourceProvider resourceProvider, String modelProjectName,
            Optional<Class<? extends Plugin>> modelProjectActivator) {
        this.resourceProvider = resourceProvider;
        this.logger = Logger.getLogger(PCMDataFlowConfidentialityAnalysis.class);
        this.modelProjectName = modelProjectName;
        this.modelProjectActivator = modelProjectActivator;
    }

    @Override
    public PCMFlowGraph findFlowGraph() {
        return new PCMFlowGraph(this.resourceProvider);
    }

    @Override
    public PCMFlowGraph evaluateFlowGraph(FlowGraph flowGraph) {
        if (!(flowGraph instanceof PCMFlowGraph)) {
            logger.error("Cannot evaluate non-pcm flow graph!", new IllegalArgumentException());
        }
        PCMFlowGraph pcmFlowGraph = (PCMFlowGraph) flowGraph;
        return pcmFlowGraph.evaluate();
    }

    @Override
    public List<? extends AbstractVertex<?>> queryDataFlow(AbstractPartialFlowGraph sequence, Predicate<? super AbstractVertex<?>> condition) {
        return sequence.getVertices().parallelStream().filter(condition).toList();
    }

    @Override
    public void initializeAnalysis() {
        if (initStandaloneAnalysis()) {
            logger.info("Successfully initialized standalone data flow analysis.");
        } else {
            throw new IllegalStateException("Standalone initialization of the data flow analysis failed.");
        }

        if (loadRequiredModels()) {
            logger.info("Successfully loaded required models for the data flow analysis.");
        } else {
            throw new IllegalStateException("Failed loading the required models for the data flow analysis.");
        }

        PCMVertexCharacteristicsCalculator nodeCharacteristicsCalculator = new PCMVertexCharacteristicsCalculator(resourceProvider);
        nodeCharacteristicsCalculator.checkAssignments();
    }

    @Override
    public void setLoggerLevel(Level level) {
        logger.setLevel(level);
        Logger.getLogger(AbstractInternalAntlrParser.class).setLevel(level);
        Logger.getLogger(DefaultLinkingService.class).setLevel(level);
        Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class).setLevel(level);
        Logger.getLogger(AbstractCleaningLinker.class).setLevel(level);
    }

    /**
     * Returns the resource provider of the analysis. The resource provider may be used to access the loaded PCM model of
     * the analysis.
     * @return Resource provider of the analysis
     */
    public ResourceProvider getResourceProvider() {
        return this.resourceProvider;
    }

    /**
     * Initializes the standalone analysis to allow logging and EMF Profiles
     * @return Returns false, if analysis could not be setup
     */
    private boolean initStandaloneAnalysis() {
        EcorePlugin.ExtensionProcessor.process(null);

        if (!setupLogLevels() || !initStandalone()) {
            return false;
        }
        DDDslStandaloneSetup.doSetup();
        return true;
    }

    /**
     * Sets up logging for the analysis
     * @return Returns true, if logging could be setup. Otherwise, the method returns false
     */
    private boolean setupLogLevels() {
        try {
            new Log4jInitilizationTask().initilizationWithoutPlatform();

            Logger.getLogger(AbstractInternalAntlrParser.class).setLevel(Level.WARN);
            Logger.getLogger(DefaultLinkingService.class).setLevel(Level.WARN);
            Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class).setLevel(Level.WARN);
            Logger.getLogger(AbstractCleaningLinker.class).setLevel(Level.WARN);

            logger.info("Successfully initialized standalone log4j for the data flow analysis");
            return true;

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone log4j for the data flow analysis", e);
            return false;
        }
    }

    /**
     * Initializes the workspace of the analysis to function without a eclipse installation
     * @return
     */
    private boolean initStandalone() {
        try {
            var initializationBuilder = StandaloneInitializerBuilder.builder()
                    .registerProjectURI(DataFlowConfidentialityAnalysis.class, DataFlowConfidentialityAnalysis.PLUGIN_PATH)
                    .registerProjectURI(PCMDataFlowConfidentialityAnalysis.class, PCMDataFlowConfidentialityAnalysis.PLUGIN_PATH);

            this.modelProjectActivator.ifPresent(projectActivator -> initializationBuilder.registerProjectURI(projectActivator, this.modelProjectName));

            initializationBuilder.build().init();

            logger.info("Successfully initialized standalone environment for the data flow analysis.");
            return true;

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone environment for the data flow analysis", e);
            return false;
        }
    }

    /**
     * Loads the required models from the resource loader
     * @return Returns true, if all required resources could be loaded. Otherwise, the method returns false
     */
    private boolean loadRequiredModels() {
        try {
            this.resourceProvider.loadRequiredResources();

            this.dataDictionaries = this.resourceProvider.lookupToplevelElement(DictionaryPackage.eINSTANCE.getPCMDataDictionary()).stream()
                    .filter(PCMDataDictionary.class::isInstance).map(PCMDataDictionary.class::cast).collect(Collectors.toList());

            logger.info(String.format("Successfully loaded %d data %s.", this.dataDictionaries.size(),
                    this.dataDictionaries.size() == 1 ? "dictionary" : "dictionaries"));
            return true;

        } catch (IllegalArgumentException e) {
            logger.error("Could not load required models", e);
            return false;
        }
    }
}
