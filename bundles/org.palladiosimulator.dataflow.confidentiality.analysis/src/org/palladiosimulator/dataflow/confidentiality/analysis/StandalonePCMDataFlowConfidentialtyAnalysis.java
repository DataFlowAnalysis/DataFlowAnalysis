package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.entity.pcm.PCMActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.resource.PCMURIResourceLoader;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.pcm.PCMActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.pcm.dddsl.DDDslStandaloneSetup;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryPackage;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class StandalonePCMDataFlowConfidentialtyAnalysis implements DataFlowConfidentialityAnalysis {
    private static final String EMF_PROFILE_NAME = "profile.emfprofile_diagram";

	private static final String EMF_PROFILE_PLUGIN = "org.palladiosimulator.dataflow.confidentiality.pcm.model.profile";

	private static final String PLUGIN_PATH = "org.palladiosimulator.dataflow.confidentiality.analysis";
	

	private final Logger logger = Logger.getLogger(StandalonePCMDataFlowConfidentialtyAnalysis.class);

    private PCMResourceLoader resourceLoader;

    private List<PCMDataDictionary> dataDictionaries;

    private String modelProjectName = "";
    private Class<? extends Plugin> modelProjectActivator;

    public StandalonePCMDataFlowConfidentialtyAnalysis(String modelProjectName,
            Class<? extends Plugin> modelProjectActivator, String relativeUsageModelPath,
            String relativeAllocationModelPath) {
        this.modelProjectName = modelProjectName;
        this.modelProjectActivator = modelProjectActivator;

        this.resourceLoader = new PCMURIResourceLoader(createRelativePluginURI(relativeUsageModelPath), 
        		createRelativePluginURI(relativeAllocationModelPath), Optional.empty());     
    }
    
    public StandalonePCMDataFlowConfidentialtyAnalysis(String modelProjectName,
            Class<? extends Plugin> modelProjectActivator, String relativeUsageModelPath,
            String relativeAllocationModelPath, String relativeNodeCharacteristicModelPath) {
        this.modelProjectName = modelProjectName;
        this.modelProjectActivator = modelProjectActivator;

        this.resourceLoader = new PCMURIResourceLoader(createRelativePluginURI(relativeUsageModelPath), 
        		createRelativePluginURI(relativeAllocationModelPath), Optional.of(createRelativePluginURI(relativeNodeCharacteristicModelPath)));     
    }

    @Override
    public List<ActionSequence> findAllSequences() {
        ActionSequenceFinder sequenceFinder = new PCMActionSequenceFinder(this.resourceLoader.getUsageModel(), 
        		this.resourceLoader.getAllocation());
        return sequenceFinder.findAllSequences().stream()
        		.map(ActionSequence.class::cast)
        		.collect(Collectors.toList());
    }

    @Override
    public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
    	List<PCMActionSequence> actionSequences = sequences.stream()
    			.map(PCMActionSequence.class::cast)
    			.collect(Collectors.toList());
    	List<PCMActionSequence> sortedSequences = new ArrayList<>(actionSequences);
    	Collections.sort(sortedSequences);
        return sortedSequences.stream()
            .map(it -> it.evaluateDataFlow(this.resourceLoader))
            .toList();
    }

    @Override
    public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
            Predicate<? super AbstractActionSequenceElement<?>> condition) {
        return sequence.getElements()
            .stream()
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
    
    public void setLoggerLevel(Level level) {
    	Logger.getRootLogger().setLevel(level);
    	Logger.getRootLogger().info("Changed log level to " + level.toString());
    }

    private boolean initStandaloneAnalysis() {
        EcorePlugin.ExtensionProcessor.process(null);

        if (!setupLogLevels() || !initStandalone() || !initEMFProfiles()) {
            return false;
        } else {
            DDDslStandaloneSetup.doSetup();
            return true;
        }
    }

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

    private boolean initStandalone() {
        try {
            StandaloneInitializerBuilder.builder()
                .registerProjectURI(this.modelProjectActivator, this.modelProjectName)
                .registerProjectURI(StandalonePCMDataFlowConfidentialtyAnalysis.class, 
                		StandalonePCMDataFlowConfidentialtyAnalysis.PLUGIN_PATH)
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

    private boolean initEMFProfiles() {
        try {
            new EMFProfileInitializationTask(StandalonePCMDataFlowConfidentialtyAnalysis.EMF_PROFILE_PLUGIN, 
            		StandalonePCMDataFlowConfidentialtyAnalysis.EMF_PROFILE_NAME)
                .initilizationWithoutPlatform();

            logger.info("Successfully initialized standalone EMF Profiles for the data flow analysis.");
            return true;

        } catch (final StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone EMF Profile for the data flow analysis.");
            e.printStackTrace();
            return false;
        }
    }

    private boolean loadRequiredModels() {
        try {
        	this.resourceLoader.loadRequiredResources();

            this.dataDictionaries = this.resourceLoader
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

    private URI createRelativePluginURI(String relativePath) {
        String path = Paths.get(this.modelProjectName, relativePath)
            .toString();
        return URI.createPlatformPluginURI(path, false);
    }

}
