package org.dataflowanalysis.converter.pcm2dfd;

import java.util.Scanner;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.resource.PCMURIResourceProvider;
import org.dataflowanalysis.converter.ConverterModel;
import org.dataflowanalysis.converter.ModelType;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.common.util.URI;

public class PCMConverterModel extends ConverterModel {
    private FlowGraphCollection flowGraphCollection;

    public PCMConverterModel(FlowGraphCollection flowGraphCollection) {
        super(ModelType.PCM);
    }

    public PCMConverterModel(String modelLocation, String usageModelPath, String allocationPath, String nodeCharPath,
            Class<? extends Plugin> activator) {
        super(ModelType.PCM);
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

        String usageModelPath = this.promptInput(scanner, "usagemodel");
        String allocationPath = this.promptInput(scanner, "allocation");
        String nodeCharPath = this.promptInput(scanner, "nodecharacteristics");

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
