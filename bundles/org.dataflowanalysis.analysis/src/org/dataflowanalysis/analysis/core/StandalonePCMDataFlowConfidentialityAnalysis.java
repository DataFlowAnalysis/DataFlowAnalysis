package org.dataflowanalysis.analysis.core;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.builder.AnalysisData;
import org.dataflowanalysis.analysis.entity.pcm.PCMActionSequence;
import org.dataflowanalysis.analysis.entity.sequence.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.entity.sequence.ActionSequence;
import org.dataflowanalysis.analysis.resource.ResourceProvider;
import org.dataflowanalysis.analysis.sequence.ActionSequenceFinder;
import org.dataflowanalysis.analysis.sequence.pcm.PCMActionSequenceFinder;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import org.dataflowanalysis.pcm.extension.dddsl.DDDslStandaloneSetup;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.DictionaryPackage;
import org.dataflowanalysis.pcm.extension.model.confidentiality.dictionary.PCMDataDictionary;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class StandalonePCMDataFlowConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private static final String PLUGIN_PATH = "org.dataflowanalysis.analysis";
	
	private final AnalysisData analysisData;
	private final Logger logger;
	
	private final String modelProjectName;
	private final Optional<Class<? extends Plugin>> modelProjectActivator;
	
	private List<PCMDataDictionary> dataDictionaries;
	
	/**
	 * Creates a new instance of an data flow analysis with the given parameters
	 * @param resourceLoader Resource loader, which loads the required model resources
	 * @param logger Logger to which error messages should be logged
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Plugin class of the analysis
	 */
	public StandalonePCMDataFlowConfidentialityAnalysis(AnalysisData analysisData, String modelProjectName,
			Optional<Class<? extends Plugin>> modelProjectActivator) {
		this.analysisData = analysisData;
		this.logger = Logger.getLogger(StandalonePCMDataFlowConfidentialityAnalysis.class);
		this.modelProjectName = modelProjectName;
		this.modelProjectActivator = modelProjectActivator;
	}

	@Override
	public List<ActionSequence> findAllSequences() {
		ActionSequenceFinder sequenceFinder = new PCMActionSequenceFinder(this.analysisData.getResourceProvider().getUsageModel());
        return sequenceFinder.findAllSequences().parallelStream()
        		.map(ActionSequence.class::cast)
        		.collect(Collectors.toList());
	}

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		List<PCMActionSequence> actionSequences = sequences.parallelStream()
    			.map(PCMActionSequence.class::cast)
    			.collect(Collectors.toList());
    	return actionSequences.parallelStream()
    	          .map(it -> it.evaluateDataFlow(this.analysisData))
    	          .toList();
	}

	@Override
	public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
			Predicate<? super AbstractActionSequenceElement<?>> condition) {
		return sequence.getElements()
	            .parallelStream()
	            .filter(condition)
	            .toList();
	}
	
	@Override
    public boolean initializeAnalysis() {
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
        
        this.analysisData.getNodeCharacteristicsCalculator().checkAssignments();

        return true;
    }
	
	@Override
	public void setLoggerLevel(Level level) {
		logger.setLevel(level);
		Logger.getLogger(AbstractInternalAntlrParser.class)
        	.setLevel(level);
		Logger.getLogger(DefaultLinkingService.class)
        	.setLevel(level);
		Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class)
        	.setLevel(level);
		Logger.getLogger(AbstractCleaningLinker.class)
        	.setLevel(level);
	}
	
	/**
	 * Returns the resource provider of the analysis.
	 * The resource provider may be used to access the loaded PCM model of the analysis.
	 * @return Resource provider of the analysis
	 */
	public ResourceProvider getResourceProvider() {
		return this.analysisData.getResourceProvider();
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

            Logger.getLogger(AbstractInternalAntlrParser.class)
                .setLevel(Level.WARN);
            Logger.getLogger(DefaultLinkingService.class)
                .setLevel(Level.WARN);
            Logger.getLogger(ResourceSetBasedAllContainersStateProvider.class)
                .setLevel(Level.WARN);
            Logger.getLogger(AbstractCleaningLinker.class)
                .setLevel(Level.WARN);

            logger.info("Successfully initialized standalone log4j for the data flow analysis.");
            return true;

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone log4j for the data flow analysis.");
            e.printStackTrace();
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
                .registerProjectURI(StandalonePCMDataFlowConfidentialityAnalysis.class, 
                		StandalonePCMDataFlowConfidentialityAnalysis.PLUGIN_PATH);
             
             if (this.modelProjectActivator.isPresent()) {
            	 initializationBuilder.registerProjectURI(this.modelProjectActivator.get(), this.modelProjectName);
             }
             
             initializationBuilder.build()
                .init();

            logger.info("Successfully initialized standalone environment for the data flow analysis.");
            return true;

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone environment for the data flow analysis.");
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Loads the required models from the resource loader
     * @return Returns true, if all required resources could be loaded. Otherwise, the method returns false
     */
    private boolean loadRequiredModels() {
        try {
        	this.analysisData.getResourceProvider().loadRequiredResources();

            this.dataDictionaries = this.analysisData.getResourceProvider()
                .lookupElementOfType(DictionaryPackage.eINSTANCE.getPCMDataDictionary())
                .stream()
                .filter(PCMDataDictionary.class::isInstance)
                .map(PCMDataDictionary.class::cast)
                .collect(Collectors.toList());

            logger.info(String.format("Successfully loaded %d data %s.", this.dataDictionaries.size(),
                    this.dataDictionaries.size() == 1 ? "dictionary" : "dictionaries"));
            return true;

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}
