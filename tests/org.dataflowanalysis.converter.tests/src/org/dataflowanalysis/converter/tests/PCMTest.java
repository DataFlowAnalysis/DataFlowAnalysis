package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.converter.PCMConverter;
import org.dataflowanalysis.converter.testmodels.Activator;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.DataCharacteristic;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
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
        String modelLocation = "org.dataflowanalysis.converter.testmodels";

        testSpecificModel("TravelPlanner", "travelPlanner", modelLocation, null, null);
    }

    @Test
    @Disabled("There is currently no manually converted pcm model")
    @DisplayName("Test manual Palladio to DFD")
    public void manualPCMToDfd() throws StandaloneInitializationException {
        String modelLocation = "org.dataflowanalysis.converter.testmodels";

        String inputModel = "InternationalOnlineShop";
        String inputFile = "default";
        String dataflowdiagram = Paths.get("models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        String datadictionary = Paths.get("models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();
        testSpecificModel(inputModel, inputFile, modelLocation, null,
                new DataFlowDiagramConverter().loadDFD(modelLocation, dataflowdiagram, datadictionary, Activator.class));

    }

    private void testSpecificModel(String inputModel, String inputFile, String modelLocation, String webTarget,
            DataFlowDiagramAndDictionary complete) {
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
                        .getId(), PCMConverter.computeCompleteName(cast));
            }
        }

        if (complete == null) {
            complete = new PCMConverter().pcmToDFD(modelLocation, usageModelPath, allocationPath, nodeCharPath, Activator.class);
        }

        if (webTarget != null) {
            var dfdConverter = new DataFlowDiagramConverter();
            var web = dfdConverter.dfdToWeb(complete);
            dfdConverter.storeWeb(web, webTarget);
        }

        DataFlowDiagram dfd = complete.dataFlowDiagram();
        var dd = complete.dataDictionary();

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
                List<DataCharacteristic> variables = ase.getAllDataCharacteristics();
                for (DataCharacteristic variable : variables) {
                    flowNames.add(variable.variableName());
                }
            }
        }

        assertEquals(flowNames.size(), dfd.getFlows()
                .size());

        // When transforming PCM to DFD, we represent all outputs through forwarding assignments.
        // This approach omits certain behaviors and labels that are not essential for visual representation.
        for (var behavior : dd.getBehaviour()) {
            for (var assignment : behavior.getAssignment()) {
                assertTrue(assignment instanceof ForwardingAssignment);
            }
        }

        checkLabels(dd, flowGraph);
    }

    private void checkLabels(DataDictionary dd, FlowGraphCollection flowGraph) {
        Map<String, CharacteristicValue> chars = new HashMap<>();
        for (var pfg : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : pfg.getVertices()) {
                for (var nodeChar : vertex.getAllVertexCharacteristics()) {
                    chars.putIfAbsent(nodeChar.getValueId(), nodeChar);
                }
            }
        }

        List<String> labelsPCM = chars.values()
                .stream()
                .map(c -> c.getTypeName() + "." + c.getValueName())
                .collect(Collectors.toList());
        List<String> labelsDFD = new ArrayList<>();

        Map<String, List<String>> labelMap = new HashMap<>();
        for (var labelType : dd.getLabelTypes()) {
            labelMap.put(labelType.getEntityName(), new ArrayList<>());
            for (var label : labelType.getLabel()) {
                var labels = labelMap.get(labelType.getEntityName());
                // prevent duplicate labels
                assertTrue(!labels.contains(label.getEntityName()));
                labels.add(label.getEntityName());
                labelMap.put(labelType.getEntityName(), labels);
                labelsDFD.add(labelType.getEntityName() + "." + label.getEntityName());
            }
        }

        Collections.sort(labelsPCM);
        Collections.sort(labelsDFD);

        assertEquals(labelsPCM, labelsDFD);
    }
}
