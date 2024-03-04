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
import org.dataflowanalysis.analysis.converter.PCMConverter;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.flowgraph.AbstractPartialFlowGraph;
import org.dataflowanalysis.analysis.flowgraph.AbstractVertex;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.flowgraph.AbstractPCMVertex;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PCMTest {
    @Test
    @DisplayName("Test Palladio to DFD")
    public void palladioToDfd() {
        String modelLocation = "org.dataflowanalysis.analysis.testmodels";

        String inputModel = "TravelPlanner";
        String inputFile = "travelPlanner";

        testSpecificModel(inputModel, inputFile, modelLocation, null);

        /*
         * inputModel = "InternationalOnlineShop"; inputFile = "default"; String dataflowdiagram = Paths.get("..",
         * modelLocation, "models", "OnlineShopDFD","onlineshop.dataflowdiagram").toString(); String datadictionary =
         * Paths.get("..", modelLocation, "models", "OnlineShopDFD","onlineshop.datadictionary").toString();
         * testSpecificModel(inputModel, inputFile, modelLocation, new DataFlowDiagramConverter().loadDFD(dataflowdiagram,
         * datadictionary));
         */

    }

    private void testSpecificModel(String inputModel, String inputFile, String modelLocation, DataFlowDiagramAndDictionary complete) {
        final var usageModelPath = Paths.get("models", inputModel, inputFile + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", inputModel, inputFile + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", inputModel, inputFile + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone().modelProjectName(modelLocation)
                .usePluginActivator(Activator.class).useUsageModel(usageModelPath).useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraph();
        var propagationResult = analysis.evaluateFlowGraph(flowGraph);

        Map<String, String> assIdToName = new HashMap<>();
        for (AbstractPartialFlowGraph aPFG : propagationResult.getPartialFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : aPFG.getVertices()) {
                var cast = (AbstractPCMVertex<?>) abstractVertex;
                assIdToName.putIfAbsent(cast.getReferencedElement().getId(), cast.getReferencedElement().getEntityName());
            }
        }

        DataFlowDiagram dfd;
        if (complete != null) {
            dfd = complete.dataFlowDiagram();
        } else {
            dfd = new PCMConverter().pcmToDFD(inputModel, inputFile, modelLocation).dataFlowDiagram();
        }

        assertEquals(dfd.getNodes().size(), assIdToName.keySet().size());

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
        for (AbstractPartialFlowGraph as : propagationResult.getPartialFlowGraphs()) {
            for (AbstractVertex<?> ase : as.getVertices()) {
                List<DataFlowVariable> variables = ase.getAllDataFlowVariables();
                for (DataFlowVariable variable : variables) {
                    flowNames.add(variable.variableName());
                }
            }
        }

        assertEquals(flowNames.size(), dfd.getFlows().size());
    }
}
