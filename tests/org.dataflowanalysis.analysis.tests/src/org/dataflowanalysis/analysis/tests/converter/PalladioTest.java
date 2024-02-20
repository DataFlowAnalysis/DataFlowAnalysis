package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.converter.PalladioConverter;
import org.dataflowanalysis.analysis.core.AbstractActionSequenceElement;
import org.dataflowanalysis.analysis.core.ActionSequence;
import org.dataflowanalysis.analysis.core.DataFlowVariable;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.seff.CallingSEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.seff.SEFFActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.CallingUserActionSequenceElement;
import org.dataflowanalysis.analysis.pcm.core.user.UserActionSequenceElement;
import org.dataflowanalysis.analysis.testmodels.Activator;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PalladioTest {
    @Test
    @DisplayName("Test Ass to DFD")
    public void assToDfd() {
        String name = "TravelPlanner";
        String modelFileName = "travelPlanner";
        String TEST_MODEL_PROJECT_NAME = "org.dataflowanalysis.analysis.testmodels";

        final var usageModelPath = Paths.get("models", name, modelFileName + ".usagemodel").toString();
        final var allocationPath = Paths.get("models", name, modelFileName + ".allocation").toString();
        final var nodeCharPath = Paths.get("models", name, modelFileName + ".nodecharacteristics").toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODEL_PROJECT_NAME).usePluginActivator(Activator.class).useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath).useNodeCharacteristicsModel(nodeCharPath).build();

        analysis.initializeAnalysis();
        analysis.findAllSequences();
        var sequences = analysis.findAllSequences();
        var propagationResult = analysis.evaluateDataFlows(sequences);

        Map<String, String> assIdToName = new HashMap<>();
        for (ActionSequence as : propagationResult) {
            for (AbstractActionSequenceElement<?> ase : as.getElements()) {
                if (ase instanceof SEFFActionSequenceElement) {
                    var cast = (SEFFActionSequenceElement<?>) ase;
                    assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
                } else if (ase instanceof CallingSEFFActionSequenceElement) {
                    var cast = (CallingSEFFActionSequenceElement) ase;
                    assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
                } else if (ase instanceof CallingUserActionSequenceElement) {
                    var cast = (CallingUserActionSequenceElement) ase;
                    assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
                } else {
                    var cast = (UserActionSequenceElement<?>) ase;
                    assIdToName.putIfAbsent(cast.getElement().getId(), cast.getElement().getEntityName());
                }
            }
        }

        PalladioConverter ass2dfd = new PalladioConverter();

        DataFlowDiagram dfd = ass2dfd.processPalladio(propagationResult).dataFlowDiagram();

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
        for (ActionSequence as : propagationResult) {
            for (AbstractActionSequenceElement<?> ase : as.getElements()) {
                List<DataFlowVariable> variables = ase.getAllDataFlowVariables();
                for (DataFlowVariable variable : variables) {
                    flowNames.add(variable.variableName());
                }
            }
        }

        assertEquals(flowNames.size(), dfd.getFlows().size());

    }
}
