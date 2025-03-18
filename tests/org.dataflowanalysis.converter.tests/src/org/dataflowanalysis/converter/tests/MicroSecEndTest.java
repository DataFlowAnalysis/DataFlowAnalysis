package org.dataflowanalysis.converter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.dataflowanalysis.converter.dfd2web.DFD2WebConverter;
import org.dataflowanalysis.converter.micro2dfd.Micro2DFDConverter;
import org.dataflowanalysis.converter.micro2dfd.MicroConverterModel;
import org.dataflowanalysis.converter.micro2dfd.model.ExternalEntity;
import org.dataflowanalysis.converter.micro2dfd.model.InformationFlow;
import org.dataflowanalysis.converter.micro2dfd.model.MicroSecEnd;
import org.dataflowanalysis.converter.micro2dfd.model.MicroSecEndProcess;
import org.dataflowanalysis.converter.micro2dfd.model.Service;
import org.dataflowanalysis.converter.plant2micro.Plant2MicroConverter;
import org.dataflowanalysis.converter.web2dfd.Web2DFDConverter;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
import org.dataflowanalysis.dfd.datadictionary.ForwardingAssignment;
import org.dataflowanalysis.dfd.datadictionary.Label;
import org.dataflowanalysis.dfd.datadictionary.LabelType;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.DataFlowDiagram;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MicroSecEndTest extends ConverterTest {
    private Micro2DFDConverter converter;

    private final String ANILALLEWAR_PATH = Paths.get(TEST_JSONS, "anilallewar.json")
            .toString();
    private final MicroConverterModel ANILALLEWAR = new MicroConverterModel(ANILALLEWAR_PATH);
    private final String TO_PLANT = Paths.get(TEST_JSONS, "toPlant.txt")
            .toString();
    private final String FROM_PLANT = Paths.get(TEST_JSONS, "fromPlant.json")
            .toString();
    private final String JSON = "json";
    private final String TXT = "txt";

    @BeforeEach
    public void setup() {
        converter = new Micro2DFDConverter();
    }

    @Test
    @DisplayName("Check amount of pins, assignments and flows")
    public void checkPinsAssignmentsAndFlows() {
        var dfd = converter.convert(ANILALLEWAR);

        List<AbstractAssignment> assignments = dfd.dataDictionary()
                .getBehavior()
                .stream()
                .flatMap(behavior -> behavior.getAssignment()
                        .stream())
                .toList();

        List<Pin> outPins = dfd.dataDictionary()
                .getBehavior()
                .stream()
                .flatMap(behavior -> behavior.getOutPin()
                        .stream())
                .toList();

        var nodes = dfd.dataFlowDiagram()
                .getNodes();
        for (Node node : nodes) {
            var behaviour = node.getBehavior();

            var forwardCount = behaviour.getAssignment()
                    .stream()
                    .filter(assignment -> assignment instanceof ForwardingAssignment)
                    .count();
            if (!behaviour.getInPin()
                    .isEmpty()) {
                assertEquals(forwardCount, behaviour.getOutPin()
                        .size());
            } else {
                assertEquals(0, forwardCount);
            }

            var assignmentCount = behaviour.getAssignment()
                    .stream()
                    .filter(assignment -> assignment instanceof Assignment)
                    .count();
            assertEquals(assignmentCount, behaviour.getOutPin()
                    .size());

            var expectedInPins = ANILALLEWAR.getModel()
                    .informationFlows()
                    .stream()
                    .map(InformationFlow::receiver)
                    .toList()
                    .contains(node.getEntityName()) ? 1 : 0;
            assertEquals(node.getBehavior()
                    .getInPin()
                    .size(), expectedInPins);
        }

        assertEquals(ANILALLEWAR.getModel()
                .informationFlows()
                .size(), outPins.size());

        // Double Check for Assignments without a output pin
        assignments.forEach(a -> {
            assert (a.getOutputPin() != null);
        });

    }

    @Test
    @DisplayName("Check if ids are not random")
    public void checkIdForRandomness() {
        var dfdConverter = new DFD2WebConverter();

        var webDfdOne = dfdConverter.convert(converter.convert(ANILALLEWAR));
        var webDfdTwo = dfdConverter.convert(converter.convert(ANILALLEWAR));

        assertEquals(webDfdOne.getModel(), webDfdTwo.getModel());
    }

    @Test
    @DisplayName("Test JSON -> Plant -> JSON")
    public void jsonToPlantToJson() throws StreamReadException, DatabindException, IOException {
        Plant2MicroConverter plant2MicroConverter = new Plant2MicroConverter();
        plant2MicroConverter.runPythonScript(ANILALLEWAR_PATH, TXT, TO_PLANT);
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(ANILALLEWAR_PATH);
        MicroSecEnd microBefore = objectMapper.readValue(file, MicroSecEnd.class);

        plant2MicroConverter.runPythonScript(TO_PLANT, JSON, FROM_PLANT);
        objectMapper = new ObjectMapper();
        file = new File(FROM_PLANT);
        MicroSecEnd microAfter = objectMapper.readValue(file, MicroSecEnd.class);

        microBefore.sort();
        microAfter.sort();

        assertEquals(microBefore, microAfter);

        cleanup(TO_PLANT);
        cleanup(FROM_PLANT);
    }

    @Test
    @DisplayName("Test Micro -> DFD")
    public void microToDfd() throws StreamReadException, DatabindException, IOException {
        org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary complete = converter.convert(ANILALLEWAR);

        DataFlowDiagram dfd = complete.dataFlowDiagram();

        assertEquals(ANILALLEWAR.getModel()
                .externalEntities()
                .size()
                + ANILALLEWAR.getModel()
                        .services()
                        .size(),
                dfd.getNodes()
                        .size());
        assertEquals(ANILALLEWAR.getModel()
                .informationFlows()
                .size(),
                dfd.getFlows()
                        .size());

        for (Service service : ANILALLEWAR.getModel()
                .services()) {
            checkEntityName(service, dfd);
        }

        for (ExternalEntity ee : ANILALLEWAR.getModel()
                .externalEntities()) {
            checkEntityName(ee, dfd);
        }

        int match = 0;
        for (InformationFlow iflow : ANILALLEWAR.getModel()
                .informationFlows()) {
            for (Flow flow : dfd.getFlows()) {
                if (iflow.sender()
                        .equals(flow.getSourceNode()
                                .getEntityName())
                        && iflow.receiver()
                                .equals(flow.getDestinationNode()
                                        .getEntityName())) {
                    Pin outpin = flow.getSourcePin();
                    List<Pin> outpins = flow.getSourceNode()
                            .getBehavior()
                            .getOutPin();
                    assertTrue(outpins.contains(outpin));
                    Assignment assignment = (Assignment) flow.getSourceNode()
                            .getBehavior()
                            .getAssignment()
                            .get(outpins.indexOf(outpin));

                    Set<String> outputNames = assignment.getOutputLabels()
                            .stream()
                            .map(label -> computeCompleteLabel(label))
                            .collect(Collectors.toSet());
                    Set<String> propertyStereotypeNames = flow.getSourceNode()
                            .getProperties()
                            .stream()
                            .filter(label -> ((LabelType) label.eContainer()).getEntityName()
                                    .equals("Stereotype"))
                            .map(label -> computeCompleteLabel(label))
                            .collect(Collectors.toSet());

                    assertTrue(propertyStereotypeNames.stream()
                            .allMatch(outputNames::contains));
                    assertTrue(iflow.stereotypes()
                            .stream()
                            .map(label -> "Stereotype." + label)
                            .allMatch(outputNames::contains));
                    for (var labelType : iflow.taggedValues()
                            .keySet()) {
                        assertTrue(iflow.taggedValues()
                                .get(labelType)
                                .stream()
                                .map(label -> labelType + "." + label)
                                .allMatch(outputNames::contains));
                    }

                    match++;
                }
            }
        }
        assertEquals(match, ANILALLEWAR.getModel()
                .informationFlows()
                .size());

        ensureCorrectDFDConversion(complete);
    }

    private String computeCompleteLabel(Label label) {
        var labelName = label.getEntityName();
        var labelType = (LabelType) label.eContainer();
        var labelTypeName = labelType.getEntityName();
        return labelTypeName + "." + labelName;
    }

    private void ensureCorrectDFDConversion(org.dataflowanalysis.converter.dfd2web.DataFlowDiagramAndDictionary complete) {
        var webConverter = new Web2DFDConverter();
        var dfdConverter = new DFD2WebConverter();
        var webBefore = dfdConverter.convert(complete);
        var webAfter = dfdConverter.convert(webConverter.convert(webBefore));
        assertEquals(webBefore.getModel(), webAfter.getModel());
    }

    private void checkEntityName(MicroSecEndProcess process, DataFlowDiagram dfd) {
        for (Node node : dfd.getNodes()) {
            if (process.name()
                    .equals(node.getEntityName())) {
                assertEquals(process.stereotypes()
                        .size()
                        + process.taggedValues()
                                .values()
                                .stream()
                                .mapToInt(List::size)
                                .sum(),
                        node.getProperties()
                                .size());
                assertEquals(process.stereotypes()
                        .size(),
                        node.getProperties()
                                .stream()
                                .filter(l -> ((LabelType) l.eContainer()).getEntityName()
                                        .equals("Stereotype"))
                                .collect(Collectors.toList())
                                .size());

                Set<String> stereotypeNames = node.getProperties()
                        .stream()
                        .map(Label::getEntityName)
                        .collect(Collectors.toSet());

                assertTrue(process.stereotypes()
                        .stream()
                        .allMatch(stereotypeNames::contains));
            }
        }
    }
}
