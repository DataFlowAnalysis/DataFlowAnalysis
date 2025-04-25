package org.dataflowanalysis.converter.pcm2dfd;

import java.util.Scanner;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.resource.PCMURIResourceProvider;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;
import org.dataflowanalysis.converter.util.PathUtils;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;

public class PCMConverterModel extends ConverterModel {
    private static final String FILE_EXTENSION_USAGE = ".usage";
    private static final String FILE_EXTENSION_ALLOCATION = ".allocation";
    private static final String FILE_EXTENSION_NODE = ".nodecharacteristics";

    private FlowGraphCollection flowGraphCollection;

    public PCMConverterModel(FlowGraphCollection flowGraphCollection) {
        super(ModelType.PCM);
    }

    public PCMConverterModel(String modelLocation, String usageModelPath, String allocationPath, String nodeCharPath,
            Class<? extends Plugin> activator) {
        super(ModelType.PCM);
        usageModelPath = PathUtils.normalizePathString(usageModelPath, FILE_EXTENSION_USAGE);
        allocationPath = PathUtils.normalizePathString(allocationPath, FILE_EXTENSION_ALLOCATION);
        nodeCharPath = PathUtils.normalizePathString(nodeCharPath, FILE_EXTENSION_NODE);
        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .usePluginActivator(activator)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.setLoggerLevel(Level.TRACE);
        analysis.initializeAnalysis();
        this.flowGraphCollection = analysis.findFlowGraphs();
        this.flowGraphCollection.evaluate();
    }

    public PCMConverterModel(String usageModelPath, String allocationPath, String nodeCharPath) {
        super(ModelType.PCM);
        usageModelPath = PathUtils.normalizePathString(usageModelPath, FILE_EXTENSION_USAGE);
        allocationPath = PathUtils.normalizePathString(allocationPath, FILE_EXTENSION_ALLOCATION);
        nodeCharPath = PathUtils.normalizePathString(nodeCharPath, FILE_EXTENSION_NODE);
        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .useCustomResourceProvider(new PCMURIResourceProvider(URI.createFileURI(usageModelPath), URI.createFileURI(allocationPath),
                        URI.createFileURI(nodeCharPath)))
                .build();

        analysis.setLoggerLevel(Level.TRACE);
        analysis.initializeAnalysis();
        this.flowGraphCollection = analysis.findFlowGraphs();
        this.flowGraphCollection.evaluate();
    }

    public PCMConverterModel(Scanner scanner) {
        super(ModelType.PCM);

        String usageModelPath = this.getFilePath(scanner, FILE_EXTENSION_USAGE);
        String allocationPath = this.getFilePath(scanner, FILE_EXTENSION_ALLOCATION);
        String nodeCharPath = this.getFilePath(scanner, FILE_EXTENSION_NODE);

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .useCustomResourceProvider(new PCMURIResourceProvider(URI.createFileURI(usageModelPath), URI.createFileURI(allocationPath),
                        URI.createFileURI(nodeCharPath)))
                .build();

        analysis.setLoggerLevel(Level.TRACE);
        analysis.initializeAnalysis();
        this.flowGraphCollection = analysis.findFlowGraphs();
        this.flowGraphCollection.evaluate();
    }

    public FlowGraphCollection getFlowGraphCollection() {
        return flowGraphCollection;
    }
}
