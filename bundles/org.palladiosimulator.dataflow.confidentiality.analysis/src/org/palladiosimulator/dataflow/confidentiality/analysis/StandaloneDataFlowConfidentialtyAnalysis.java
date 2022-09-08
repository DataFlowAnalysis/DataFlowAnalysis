package org.palladiosimulator.dataflow.confidentiality.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.linking.impl.AbstractCleaningLinker;
import org.eclipse.xtext.linking.impl.DefaultLinkingService;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.resource.containers.ResourceSetBasedAllContainersStateProvider;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.ActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.PCMActionSequenceFinder;
import org.palladiosimulator.dataflow.confidentiality.analysis.sequence.entity.ActionSequence;
import org.palladiosimulator.dataflow.confidentiality.pcm.dddsl.DDDslStandaloneSetup;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.DictionaryPackage;
import org.palladiosimulator.dataflow.confidentiality.pcm.model.confidentiality.dictionary.PCMDataDictionary;
import org.palladiosimulator.pcm.allocation.Allocation;
import org.palladiosimulator.pcm.usagemodel.UsageModel;

import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;
import tools.mdsd.library.standalone.initialization.StandaloneInitializerBuilder;
import tools.mdsd.library.standalone.initialization.emfprofiles.EMFProfileInitializationTask;
import tools.mdsd.library.standalone.initialization.log4j.Log4jInitilizationTask;

public class StandaloneDataFlowConfidentialtyAnalysis implements DataFlowConfidentialityAnalysis {
    private static final String EMF_PROFILE_PLUGIN = "org.palladiosimulator.dataflow.confidentiality.pcm.model.profile";
    private static final String EMF_PROFILE_NAME = "profile.emfprofile_diagram";
    private final static String PLUGIN_PATH = "org.palladiosimulator.dataflow.confidentiality.analysis";

    private final Logger logger = Logger.getLogger(StandaloneDataFlowConfidentialtyAnalysis.class);
    private final ResourceSet resourceSet = new ResourceSetImpl();

    private final URI usageModelURI;
    private final URI allocationModelURI;

    private Allocation allocationModel = null;
    private UsageModel usageModel = null;
    private List<PCMDataDictionary> dataDictionaries;

    public StandaloneDataFlowConfidentialtyAnalysis(String relativeUsageModelPath, String relativeAllocationModelPath) {
        this.usageModelURI = getRelativePluginURI(relativeUsageModelPath);
        this.allocationModelURI = getRelativePluginURI(relativeAllocationModelPath);
    }

    @Override
    public boolean initalizeAnalysis() {
        if (initStandaloneAnalysis()) {
            logger.info("Successfully initialized standalone data flow analysis.");
            return true;
        } else {
            logger.warn("Standalone initialization of the data flow analysis failed.");
            return false;
        }
    }

    @Override
    public boolean loadModels() {
        if (loadRequiredModels()) {
            logger.info("Successfully loaded required models for the data flow analysis.");
            return true;
        } else {
            logger.warn("Failed loading the required models for the data flow analysis.");
            return false;
        }
    }

    @Override
    public List<ActionSequence> findAllSequences() {
        ActionSequenceFinder sequenceFinder = new PCMActionSequenceFinder(usageModel, allocationModel);
        return sequenceFinder.findAllSequences();
    }

    private boolean initStandaloneAnalysis() {
        EcorePlugin.ExtensionProcessor.process(null);

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

        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone log4j for the data flow analysis.");
            e.printStackTrace();
            return false;
        }

        try {
            StandaloneInitializerBuilder.builder()
                .registerProjectURI(StandaloneDataFlowConfidentialtyAnalysis.class, PLUGIN_PATH)
                .build()
                .init();
            logger.info("Successfully initialized standalone environment for the data flow analysis.");
        } catch (StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone environment for the data flow analysis.");
            e.printStackTrace();
            return false;
        }

        try {
            new EMFProfileInitializationTask(EMF_PROFILE_PLUGIN, EMF_PROFILE_NAME).initilizationWithoutPlatform();
            logger.info("Successfully initialized standalone EMF Profiles for the data flow analysis.");
        } catch (final StandaloneInitializationException e) {
            logger.error("Unable to initialize standalone EMF Profile for the data flow analysis.");
            e.printStackTrace();
            return false;
        }

        DDDslStandaloneSetup.doSetup();
        return true;
    }

    private boolean loadRequiredModels() {
        try {
            this.usageModel = (UsageModel) loadModelContent(usageModelURI);
            this.allocationModel = (Allocation) loadModelContent(allocationModelURI);

            logger.info("Successfully loaded usage model and allocation model.");

            // This is required to load other models like data dictionaries
            resolveAllProxies();

            this.dataDictionaries = lookupElementOfType(DictionaryPackage.eINSTANCE.getPCMDataDictionary()).stream()
                .filter(PCMDataDictionary.class::isInstance)
                .map(PCMDataDictionary.class::cast)
                .collect(Collectors.toList());

            logger.info(String.format("Successfully loaded %d data %s.", this.dataDictionaries.size(),
                    this.dataDictionaries.size() > 1 ? "dictionaries" : "dictionary"));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // Partially based on Palladio's ResourceSetPartition
    private EObject loadModelContent(URI modelURI) {
        final Resource resource = this.resourceSet.getResource(modelURI, true);

        if (resource == null) {
            throw new IllegalArgumentException(String.format("Model with URI %s could not be loaded.", modelURI));
        } else if (resource.getContents()
            .size() == 0) {
            throw new IllegalArgumentException(String.format("Model with URI %s is empty.", modelURI));
        }
        return resource.getContents()
            .get(0);
    }

    // Partially based on Palladio's ResourceSetPartition
    private <T extends EObject> List<T> lookupElementOfType(final EClass targetType) {
        final ArrayList<T> result = new ArrayList<T>();
        for (final Resource r : this.resourceSet.getResources()) {
            if (this.isTargetInResource(targetType, r)) {
                result.addAll(EcoreUtil.<T> getObjectsByType(r.getContents(), targetType));
            }
        }

        return result;
    }

    // Partially based on Palladio's ResourceSetPartition
    private boolean isTargetInResource(final EClass targetType, final Resource resource) {
        if (resource != null) {
            for (EObject c : resource.getContents()) {
                if (targetType.isSuperTypeOf(c.eClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Partially based on Palladio's ResourceSetPartition
    private void resolveAllProxies() {
        ArrayList<Resource> currentResources = null;
        int initialResourceCount = this.resourceSet.getResources()
            .size();

        do {
            currentResources = new ArrayList<Resource>(this.resourceSet.getResources());
            for (final Resource r : currentResources) {
                EcoreUtil.resolveAll(r);
            }
        } while (currentResources.size() != this.resourceSet.getResources()
            .size());

        int additionalResourceCount = this.resourceSet.getResources()
            .size() - initialResourceCount;
        logger.info(String.format("Successfully resolved %d additional resources.", additionalResourceCount));
    }

    private URI getRelativePluginURI(String relativePath) {
        // FIXME: Might not be platform independent enough although it works on windows
        return URI.createPlatformPluginURI("/" + PLUGIN_PATH + "/" + relativePath, false);
    }

}
