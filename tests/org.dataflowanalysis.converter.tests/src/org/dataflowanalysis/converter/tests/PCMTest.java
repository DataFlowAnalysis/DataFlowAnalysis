package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Level;
import org.dataflowanalysis.analysis.DataFlowConfidentialityAnalysis;
import org.dataflowanalysis.analysis.core.AbstractTransposeFlowGraph;
import org.dataflowanalysis.analysis.core.AbstractVertex;
import org.dataflowanalysis.analysis.core.CharacteristicValue;
import org.dataflowanalysis.analysis.core.FlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.core.DFDFlowGraphCollection;
import org.dataflowanalysis.analysis.dfd.simple.DFDSimpleTransposeFlowGraphFinder;
import org.dataflowanalysis.analysis.dsl.AnalysisConstraint;
import org.dataflowanalysis.analysis.dsl.constraint.ConstraintDSL;
import org.dataflowanalysis.analysis.dsl.selectors.Intersection;
import org.dataflowanalysis.analysis.dsl.variable.ConstraintVariable;
import org.dataflowanalysis.analysis.pcm.PCMDataFlowConfidentialityAnalysisBuilder;
import org.dataflowanalysis.analysis.pcm.core.AbstractPCMVertex;
import org.dataflowanalysis.converter.dfd2web.DFD2WebConverter;
import org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.converter.pcm2dfd.PCM2DFDConverter;
import org.dataflowanalysis.converter.pcm2dfd.PCMConverterModel;
import org.dataflowanalysis.dfd.datadictionary.AND;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.DataDictionary;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.LabelReference;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.dataflowanalysis.examplemodels.Activator;
import org.eclipse.core.runtime.Plugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PCMTest extends ConverterTest {
    @Test
    @DisplayName("Test PCM2DFD TravelPlanner")
    public void travelToDfd() {
        testSpecificModel("TravelPlanner", "travelPlanner", TEST_MODELS, "tp.json", getTravelPlannerConstraints());
    }

    @Test
    @DisplayName("Test PCM2DFD MaaS")
    public void maasToDfd() {
        testSpecificModel("MaaSTicketSystem", "MaaS", TEST_MODELS, "maas.json", getMaasConstraints());
    }

    @Test
    @DisplayName("Test PCM2DFD CWA")
    public void cwaToDfd() {
        testSpecificModel("CoronaWarnApp", "default", TEST_MODELS, "cwa.json", null);
    }

    @Test
    @DisplayName("Test PCM2DFD TravelPlanner Behavior")
    public void testTravelPlannerBehavior() {
        final var usageModelPath = Paths.get("scenarios", "pcm", "TravelPlanner", "travelPlanner.usagemodel")
                .toString();
        final var allocationPath = Paths.get("scenarios", "pcm", "TravelPlanner", "travelPlanner.allocation")
                .toString();
        final var nodeCharPath = Paths.get("scenarios", "pcm", "TravelPlanner", "travelPlanner.nodecharacteristics")
                .toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(TEST_MODELS)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.setLoggerLevel(Level.ALL);

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        List<AbstractPCMVertex<?>> vertices = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
                AbstractPCMVertex<?> v = (AbstractPCMVertex<?>) abstractVertex;
                vertices.add(v);
            }
        }

        PCMConverterModel pcmConverterModel = new PCMConverterModel(TEST_MODELS, usageModelPath, allocationPath, nodeCharPath, Activator.class);
        var dfd = new PCM2DFDConverter().convert(pcmConverterModel);

        // Assignment: flights.*.* := RETURN.*.*
        var readFlightsFromDB = dfd.dataFlowDiagram()
                .getNodes()
                .stream()
                .filter(it -> it.getId()
                        .equals("_x32bcPViEeuMKba1Qn68bg_1"))
                .findAny()
                .orElseThrow();
        assertTrue(readFlightsFromDB.getBehavior()
                .getAssignment()
                .stream()
                .filter(ForwardingAssignment.class::isInstance)
                .filter(it -> ((ForwardingAssignment) it).getInputPins()
                        .size() == 1)
                .filter(it -> ((ForwardingAssignment) it).getInputPins()
                        .get(0)
                        .getEntityName()
                        .equals("RETURN"))
                .anyMatch(it -> it.getOutputPin()
                        .getEntityName()
                        .equals("flights")));

        // Assignment: RETURN.GrantedRoles.* := query.GrantedRoles.* & flight.GrantedRoles.*
        var selectFlightsBasedOnQuery = dfd.dataFlowDiagram()
                .getNodes()
                .stream()
                .filter(it -> it.getId()
                        .equals("_2AAjoPViEeuMKba1Qn68bg"))
                .findAny()
                .orElseThrow();
        assertEquals(2, selectFlightsBasedOnQuery.getBehavior()
                .getAssignment()
                .stream()
                .filter(Assignment.class::isInstance)
                .map(Assignment.class::cast)
                .filter(it -> it.getOutputPin()
                        .getEntityName()
                        .equals("RETURN"))
                .filter(it -> it.getOutputLabels()
                        .size() == 1)
                .filter(it -> ((LabelType) it.getOutputLabels()
                        .get(0)
                        .eContainer()).getEntityName()
                                .equals("GrantedRoles"))
                .map(it -> it.getTerm())
                .filter(AND.class::isInstance)
                .map(AND.class::cast)
                .filter(it -> it.getTerms()
                        .size() == 2)
                .filter(it -> ((LabelType) ((LabelReference) it.getTerms()
                        .get(0)).getLabel()
                                .eContainer()).getEntityName()
                                        .equals("GrantedRoles"))
                .filter(it -> ((LabelType) ((LabelReference) it.getTerms()
                        .get(1)).getLabel()
                                .eContainer()).getEntityName()
                                        .equals("GrantedRoles"))
                .toList()
                .size());
    }

    private void testSpecificModel(String inputModel, String inputFile, String modelLocation, String webTarget,
            List<AnalysisConstraint> constraints) {
        final var usageModelPath = Paths.get("scenarios", "pcm", inputModel, inputFile + ".usagemodel")
                .toString();
        final var allocationPath = Paths.get("scenarios", "pcm", inputModel, inputFile + ".allocation")
                .toString();
        final var nodeCharPath = Paths.get("scenarios", "pcm", inputModel, inputFile + ".nodecharacteristics")
                .toString();

        DataFlowConfidentialityAnalysis analysis = new PCMDataFlowConfidentialityAnalysisBuilder().standalone()
                .modelProjectName(modelLocation)
                .usePluginActivator(Activator.class)
                .useUsageModel(usageModelPath)
                .useAllocationModel(allocationPath)
                .useNodeCharacteristicsModel(nodeCharPath)
                .build();

        analysis.setLoggerLevel(Level.ALL);

        analysis.initializeAnalysis();
        var flowGraph = analysis.findFlowGraphs();
        flowGraph.evaluate();

        List<AbstractPCMVertex<?>> vertices = new ArrayList<>();
        for (AbstractTransposeFlowGraph transposeFlowGraph : flowGraph.getTransposeFlowGraphs()) {
            for (AbstractVertex<?> abstractVertex : transposeFlowGraph.getVertices()) {
                AbstractPCMVertex<?> v = (AbstractPCMVertex<?>) abstractVertex;
                vertices.add(v);
            }
        }

        PCMConverterModel pcmConverterModel = new PCMConverterModel(modelLocation, usageModelPath, allocationPath, nodeCharPath, Activator.class);
        var complete = new PCM2DFDConverter().convert(pcmConverterModel);

        var dfd2WebConverter = new DFD2WebConverter();
        dfd2WebConverter.setConditions(constraints);
        dfd2WebConverter.setTransposeFlowGraphFinder(DFDSimpleTransposeFlowGraphFinder.class);
        var web = dfd2WebConverter.convert(complete);
        web.save(".", webTarget);

        var dfd = complete.dataFlowDiagram();
        var dd = complete.dataDictionary();

        assertEquals(dfd.getNodes()
                .size(), vertices.size());

        if (constraints != null && !constraints.isEmpty()) {
            DFDSimpleTransposeFlowGraphFinder dfdTransposeFlowGraphFinder = new DFDSimpleTransposeFlowGraphFinder(dd, dfd);
            var dfdTFGCollection = new DFDFlowGraphCollection(null, dfdTransposeFlowGraphFinder.findTransposeFlowGraphs()
                    .stream()
                    .map(it -> {
                        return it.evaluate();
                    })
                    .toList());
            List<String> nodeIds = new ArrayList<>();
            for (Node node : dfd.getNodes()) {
                nodeIds.add(node.getId());
            }

            var results = constraints.stream()
                    .flatMap(constraint -> constraint.findViolations(flowGraph)
                            .stream())
                    .toList();

            var dfdResults = constraints.stream()
                    .flatMap(constraint -> constraint.findViolations(dfdTFGCollection)
                            .stream())
                    .toList();

            assertEquals(results.size(), dfdResults.size());
            checkTFGs(flowGraph, dfdTFGCollection);
        }

        checkLabels(dd, flowGraph);
        checkIDPreserving(flowGraph, dfd);
        checkNames(flowGraph, dfd);
    }

    private void checkIDPreserving(FlowGraphCollection pcmFlowGraphs, DataFlowDiagram dfd) {
        List<String> ids = pcmFlowGraphs.getTransposeFlowGraphs()
                .stream()
                .map(AbstractTransposeFlowGraph::getVertices)
                .flatMap(List::stream)
                .filter(it -> it instanceof AbstractPCMVertex<?>)
                .map(it -> (AbstractPCMVertex<?>) it)
                .map(it -> it.getReferencedElement()
                        .getId())
                .toList();
        List<Node> nodes = dfd.getNodes();
        for (Node node : nodes) {
            String dfdId = node.getId();
            if (ids.contains(dfdId)) {
                continue;
            }
            int suffixIndex = dfdId.lastIndexOf('_');
            String strippedId = dfdId.substring(0, suffixIndex);
            assertTrue(ids.contains(strippedId), "Could not find PCM Vertex with ID: " + dfdId + " / " + strippedId);
        }
    }

    private void checkNames(FlowGraphCollection pcmFlowGraphs, DataFlowDiagram dfd) {
        var vertices = pcmFlowGraphs.getTransposeFlowGraphs()
                .stream()
                .map(AbstractTransposeFlowGraph::getVertices)
                .flatMap(List::stream)
                .filter(it -> it instanceof AbstractPCMVertex<?>)
                .map(it -> (AbstractPCMVertex<?>) it)
                .toList();

        Map<String, String> nameMapping = new HashMap<>();

        for (var vertex : vertices) {
            nameMapping.putIfAbsent(vertex.getReferencedElement()
                    .getId(),
                    vertex.getReferencedElement()
                            .getEntityName());
        }

        List<Node> nodes = dfd.getNodes();
        for (Node node : nodes) {
            String dfdId = node.getId();
            String nodeNameStripped = node.getEntityName()
                    .replace("Calling ", "")
                    .replace("Returning ", "");
            if (nameMapping.containsKey(dfdId)) {
                if (nameMapping.get(dfdId)
                        .equals("aName")) {
                    continue;
                }
                assertEquals(nameMapping.get(dfdId), nodeNameStripped);
                continue;
            }
            int suffixIndex = dfdId.lastIndexOf('_');
            String strippedId = dfdId.substring(0, suffixIndex);

            if (!nameMapping.containsKey(strippedId)) {
                fail("Could not find PCM Vertex with the transformed DFD IDs: " + dfdId + " / " + strippedId);
            }
            if (nameMapping.get(strippedId)
                    .equals("aName")) {
                continue;
            }
            assertEquals(nodeNameStripped, nameMapping.get(strippedId), "Could not find PCM Vertex with ID: " + dfdId + " / " + strippedId);
        }
    }

    private void checkTFGs(FlowGraphCollection pcmFlowGraphs, FlowGraphCollection dfdFlowGraphs) {
        assertEquals(pcmFlowGraphs.getTransposeFlowGraphs()
                .size(),
                dfdFlowGraphs.getTransposeFlowGraphs()
                        .size());
    }

    private void checkLabels(DataDictionary dd, FlowGraphCollection flowGraph) {
        Set<CharacteristicValue> values = new HashSet<>();
        for (var pfg : flowGraph.getTransposeFlowGraphs()) {
            for (var vertex : pfg.getVertices()) {
                for (var nodeChar : vertex.getAllVertexCharacteristics()) {
                    values.add(nodeChar);
                }
                for (var dataChar : vertex.getAllIncomingDataCharacteristics()) {
                    for (var charValue : dataChar.getAllCharacteristics()) {
                        values.add(charValue);
                    }
                }
            }
        }

        List<String> labelsPCM = values.stream()
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

    private static Stream<Arguments> getPCMModels() {
        return Stream.of(Arguments.of(TEST_MODELS, "scenarios/pcm/CoCarNextGen/AudiA6C8.usagemodel", "scenarios/pcm/CoCarNextGen/AudiA6C8.allocation",
                "scenarios/pcm/CoCarNextGen/AudiA6C8.nodecharacteristics", Activator.class));
    }

    @ParameterizedTest
    @MethodSource("getPCMModels")
    public void testValidDFD(String modelLocation, String usageModelPath, String allocationPath, String nodeCharPath,
            Class<? extends Plugin> activator) {
        PCM2DFDConverter converter = new PCM2DFDConverter();
        PCMConverterModel converterModel = new PCMConverterModel(modelLocation, usageModelPath, allocationPath, nodeCharPath, activator);
        DataFlowDiagramAndDictionary dfd = converter.convert(converterModel);
        for (Node node : dfd.dataFlowDiagram()
                .getNodes()) {
            for (AbstractAssignment abstractAssignment : node.getBehavior()
                    .getAssignment()) {
                if (abstractAssignment instanceof Assignment assignment) {
                    if (assignment.getInputPins()
                            .isEmpty() && assignment.getOutputPin() == null) {
                        System.err.println(node);
                    }
                    assertFalse(assignment.getInputPins()
                            .isEmpty() && assignment.getOutputPin() == null, "Invalid node" + node);
                }
            }
        }
    }

    private List<AnalysisConstraint> getTravelPlannerConstraints() {
        return List.of(new ConstraintDSL().ofData()
                .withLabel("GrantedRoles", ConstraintVariable.of("grantedRoles"))
                .neverFlows()
                .toVertex()
                .withCharacteristic("AssignedRoles", ConstraintVariable.of("assignedRoles"))
                .where()
                .isNotEmpty(ConstraintVariable.of("grantedRoles"))
                .isNotEmpty(ConstraintVariable.of("assignedRoles"))
                .isEmpty(Intersection.of(ConstraintVariable.of("grantedRoles"), ConstraintVariable.of("assignedRoles")))
                .create());
    }

    public List<AnalysisConstraint> getMaasConstraints() {
        List<AnalysisConstraint> constraints = new ArrayList<>();
        constraints.add(new ConstraintDSL().fromNode()
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "MaliciousActor")
                .create());
        constraints.add(new ConstraintDSL().ofData()
                .withLabel("DataType", "LoginData")
                .neverFlows()
                .toVertex()
                .withCharacteristic("Role", "Customer")
                .create());
        constraints.add(new ConstraintDSL().ofData()
                .withLabel("Origin", "Leaked")
                .neverFlows()
                .toVertex()
                .create());
        return constraints;
    }
}
