package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.PCMConverter;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.mdsd.library.standalone.initialization.StandaloneInitializationException;

public class PCMTest {
    @Test
    @DisplayName("Test Palladio to DFD")
    public void palladioToDfd() {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        String inputModel = "TravelPlanner";
        String inputFile = "travelPlanner";

        testSpecificModel(inputModel, inputFile, modelLocation, null);
    }

    @Test
    @Disabled("There is currently no manually converted pcm model")
    @DisplayName("Test manual Palladio to DFD")
    public void manualPCMToDfd() throws StandaloneInitializationException {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        String inputModel = "InternationalOnlineShop";
        String inputFile = "default";
        String dataflowdiagram = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        String datadictionary = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();
        testSpecificModel(inputModel, inputFile, modelLocation,
                new DataFlowDiagramConverter().loadDFD(modelLocation, dataflowdiagram, datadictionary, Activator.class));

    }

    private void testSpecificModel(String inputModel, String inputFile, String modelLocation, DataFlowDiagramAndDictionary complete) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel")
                .toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation")
                .toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics")
                .toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        Map<String, String> assIdToName = new HashMap<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
                var cast = (AbstractPCMVertex<?>) abstractVertex;
                assIdToName.putIfAbsent(cast.getReferencedElement()
                        .getId(),
                        cast.getReferencedElement()
                                .getEntityName());
            }
        }

        DataFlowDiagram dfd;
        if (complete != null) {
            dfd = complete.dataFlowDiagram();
        } else {
            dfd = new PCMConverter().pcmToDFD(modelLocation, usageModelPath, allocationPath, nodeCharPath, Activator.class)
                    .dataFlowDiagram();
        }

        assertEquals(dfd.getNodes()
                .size(),
                assIdToName.keySet()
                        .size());

        List<String> nodeIds = new ArrayList<>();
        for (Node node : dfd.getNodes()) {
            nodeIds.add(node.getId());
        }
        Collections.sort(nodeIds);
        List<String> assIds = new ArrayList<>(assIdToName.keySet());
        Collections.sort(assIds);

        assertEquals(assIds, nodeIds);

        for (Node node : dfd.getNodes()) {
            assertEquals(node.getEntityName(), assIdToName.get(node.getId()));
        }

        List<String> flowNames = new ArrayList<>();
        for (AbstractTransposeFlowGraph as : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> ase : as.getVertices()) {
                List<DataFlowVariable> variables = ase.getAllDataFlowVariables();
                for (DataFlowVariable variable : variables) {
                    flowNames.add(variable.variableName());
                }
            }
        }

        assertEquals(flowNames.size(), dfd.getFlows()
                .size());
    }
}
