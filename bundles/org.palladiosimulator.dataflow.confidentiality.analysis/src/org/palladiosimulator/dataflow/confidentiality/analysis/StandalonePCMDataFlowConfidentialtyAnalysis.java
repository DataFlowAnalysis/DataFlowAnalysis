package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.AbstractActionSequenceElement;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.pcm.DatabaseActionSequenceElement;
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
    private final Logger logger = Logger.getLogger(StandalonePCMDataFlowConfidentialtyAnalysis.class);

    private final URI usageModelURI;
    private UsageModel usageModel;

    private Allocation allocationModel;
    private final URI allocationModelURI;

    private List<PCMDataDictionary> dataDictionaries;

    private String modelProjectName = "";
    private Class<? extends Plugin> modelProjectActivator;

    public StandalonePCMDataFlowConfidentialtyAnalysis(String modelProjectName,
            Class<? extends Plugin> modelProjectActivator, String relativeUsageModelPath,
            String relativeAllocationModelPath) {
        this.modelProjectName = modelProjectName;
        this.modelProjectActivator = modelProjectActivator;

        this.usageModelURI = createRelativePluginURI(relativeUsageModelPath);
        this.allocationModelURI = createRelativePluginURI(relativeAllocationModelPath);
    }

    @Override
    public List<ActionSequence> findAllSequences() {
        ActionSequenceFinder sequenceFinder = new PCMActionSequenceFinder(usageModel, allocationModel);
        return sequenceFinder.findAllSequences();
    }

    @Override
    public List<ActionSequence> evaluateDataFlows(List<ActionSequence> sequences) {
        return sequences.stream()
            .map(it -> it.evaluateDataFlow())
            .toList();
    }

    @Override
    public List<AbstractActionSequenceElement<?>> queryDataFlow(ActionSequence sequence,
            Predicate<? super AbstractActionSequenceElement<?>> condition) {
        return sequence.elements()
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
                .registerProjectURI(StandalonePCMDataFlowConfidentialtyAnalysis.class, PCMAnalysisUtils.PLUGIN_PATH)
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
            new EMFProfileInitializationTask(PCMAnalysisUtils.EMF_PROFILE_PLUGIN, PCMAnalysisUtils.EMF_PROFILE_NAME)
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
            this.usageModel = (UsageModel) PCMAnalysisUtils.loadModelContent(usageModelURI);
            this.allocationModel = (Allocation) PCMAnalysisUtils.loadModelContent(allocationModelURI);

            logger.info("Successfully loaded usage model and allocation model.");

            PCMAnalysisUtils.resolveAllProxies();

            this.dataDictionaries = PCMAnalysisUtils
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
