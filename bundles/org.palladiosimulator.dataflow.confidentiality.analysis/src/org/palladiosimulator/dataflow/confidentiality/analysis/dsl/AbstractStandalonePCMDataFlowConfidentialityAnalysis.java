package org.palladiosimulator.dataflow.confidentiality.analysis.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import org.palladiosimulator.dataflow.confidentiality.analysis.DataFlowConfidentialityAnalysis;
import org.palladiosimulator.dataflow.confidentiality.analysis.builder.AnalysisData;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.sequence.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.utils.pcm.AnalysisConstants;
import org.palladiosimulator.dataflow.confidentiality.pcm.dddsl.DDDslStandaloneSetup;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryPackage;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public abstract class AbstractStandalonePCMDataFlowConfidentialityAnalysis implements DataFlowConfidentialityAnalysis {
	private final AnalysisData analysisData;
	private final Logger logger;
	
	private final String modelProjectName;
	private final Class<? extends Plugin> modelProjectActivator;
	
	private List<PCMDataDictionary> dataDictionaries;
	
	/**
	 * Creates a new instance of an data flow analysis with the given parameters
	 * @param resourceLoader Resource loader, which loads the required model resources
	 * @param logger Logger to which error messages should be logged
	 * @param modelProjectName Name of the modelling project
	 * @param modelProjectActivator Plugin class of the analysis
	 */
	public AbstractStandalonePCMDataFlowConfidentialityAnalysis(AnalysisData analysisData, Logger logger, String modelProjectName,
			Class<? extends Plugin> modelProjectActivator) {
		this.analysisData = analysisData;
		this.logger = logger;
		this.modelProjectName = modelProjectName;
		this.modelProjectActivator = modelProjectActivator;
	}
	
	/**
	 * Performs additional tasks when the analysis is initialized. Errors are printed to the provided logger
	 * @return Returns true, when initialization is successful. Otherwise, the method should return false.
	 */
	public abstract boolean setupAnalysis();

	@Override
	public List<ActionSequence> findAllSequences() {
		ActionSequenceFinder sequenceFinder = new PCMActionSequenceFinder(this.analysisData.getResourceLoader().getUsageModel());
        return sequenceFinder.findAllSequences().parallelStream()
        		.map(ActionSequence.class::cast)
        		.collect(Collectors.toList());
	}

	@Override
	public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
		List<PCMActionSequence> actionSequences = sequences.parallelStream()
    			.map(PCMActionSequence.class::cast)
    			.collect(Collectors.toList());
    	List<PCMActionSequence> sortedSequences = new ArrayList<>(actionSequences);
    	Collections.sort(sortedSequences);
    	// TODO: Normal stream here due to DatastoreComponents (Different behavior/race condition with store/get)
        return sortedSequences.stream()
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
    public boolean initalizeAnalysis() {
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
	 * Initializes the standalone analysis to allow logging and EMF Profiles
	 * @return Returns false, if analysis could not be setup
	 */
    private boolean initStandaloneAnalysis() {
        EcorePlugin.ExtensionProcessor.process(null);

        if (!setupLogLevels() || !initStandalone() || !setupAnalysis()) {
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
            StandaloneInitializerBuilder.builder()
                .registerProjectURI(this.modelProjectActivator, this.modelProjectName)
                .registerProjectURI(AbstractStandalonePCMDataFlowConfidentialityAnalysis.class, 
                		AnalysisConstants.PLUGIN_PATH)
                .build()
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
        	this.analysisData.getResourceLoader().loadRequiredResources();

            this.dataDictionaries = this.analysisData.getResourceLoader()
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
