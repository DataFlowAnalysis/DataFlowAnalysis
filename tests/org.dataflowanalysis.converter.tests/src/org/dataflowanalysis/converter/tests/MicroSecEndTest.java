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
import org.dataflowanalysis.converter.*;
import org.dataflowanalysis.converter.microsecend.*;
import org.dataflowanalysis.dfd.datadictionary.AbstractAssignment;
import org.dataflowanalysis.dfd.datadictionary.Assignment;
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
    private MicroSecEndConverter converter;

    private final String ANILALLEWAR = Paths.get(packagePath, "anilallewar.json")
            .toString();
    private final String TO_PLANT = Paths.get(packagePath, "toPlant.txt")
            .toString();
    private final String FROM_PLANT = Paths.get(packagePath, "fromPlant.json")
            .toString();
    private final String JSON = "json";
    private final String TXT = "txt";

    @BeforeEach
    public void setup() {
        converter = new MicroSecEndConverter();
    }

    @Test
    @DisplayName("Check amout of pins, assigments and flows")
    public void checkPinsAssigmentsAndFlows() {
        var micro = converter.loadMicro(ANILALLEWAR)
                .get();
        var dfd = converter.microToDfd(micro);

        List<AbstractAssignment> assignments = dfd.dataDictionary()
                .getBehaviour()
                .stream()
                .flatMap(behavior -> behavior.getAssignment()
                        .stream())
                .collect(Collectors.toList());

        List<Pin> outPins = dfd.dataDictionary()
                .getBehaviour()
                .stream()
                .flatMap(behavior -> behavior.getOutPin()
                        .stream())
                .collect(Collectors.toList());

        List<Pin> inPins = dfd.dataDictionary()
                .getBehaviour()
                .stream()
                .flatMap(behavior -> behavior.getInPin()
                        .stream())
                .collect(Collectors.toList());

        // One Node assignment and one Forwarding assignment per flow
        assertEquals(micro.informationFlows()
                .size() * 2, assignments.size());
        assertEquals(micro.informationFlows()
                .size(), outPins.size());
        assertEquals(outPins.size(), inPins.size());

        // Double Check for Assignments without a output pin
        assignments.forEach(a -> {
            assert (a.getOutputPin() != null);
        });

    }

    @Test
    @DisplayName("Check if ids are not random")
    public void checkIdForRandomness() {
        var dfdConverter = new DataFlowDiagramConverter();

        var webDfdOne = dfdConverter.dfdToWeb(converter.microToDfd(ANILALLEWAR));
        var webDfdTwo = dfdConverter.dfdToWeb(converter.microToDfd(ANILALLEWAR));

        assertEquals(webDfdOne, webDfdTwo);
    }

    @Test
    @DisplayName("Test JSON -> Plant -> JSON")
    public void jsonToPlantToJson() throws StreamReadException, DatabindException, IOException {
        converter.runPythonScript(ANILALLEWAR, TXT, TO_PLANT);
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(ANILALLEWAR);
        MicroSecEnd microBefore = objectMapper.readValue(file, MicroSecEnd.class);

        converter.runPythonScript(TO_PLANT, JSON, FROM_PLANT);
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
        MicroSecEnd micro = converter.loadMicro(ANILALLEWAR)
                .get();
        DataFlowDiagramAndDictionary complete = converter.microToDfd(micro);

        DataFlowDiagram dfd = complete.dataFlowDiagram();

        assertEquals(micro.externalEntities()
                .size()
                + micro.services()
                        .size(),
                dfd.getNodes()
                        .size());
        assertEquals(micro.informationFlows()
                .size(),
                dfd.getFlows()
                        .size());

        for (Service service : micro.services()) {
            checkEntityName(service, dfd);
        }

        for (ExternalEntity ee : micro.externalEntities()) {
            checkEntityName(ee, dfd);
        }

        int match = 0;
        for (InformationFlow iflow : micro.informationFlows()) {
            for (Flow flow : dfd.getFlows()) {
                if (iflow.sender()
                        .equals(flow.getSourceNode()
                                .getEntityName())
                        && iflow.receiver()
                                .equals(flow.getDestinationNode()
                                        .getEntityName())) {
                    Pin outpin = flow.getSourcePin();
                    List<Pin> outpins = flow.getSourceNode()
                            .getBehaviour()
                            .getOutPin();
                    assertTrue(outpins.contains(outpin));
                    Assignment assignment = (Assignment) flow.getSourceNode()
                            .getBehaviour()
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
        assertEquals(match, micro.informationFlows()
                .size());

        ensureCorrectDFDConversion(complete);
    }

    private String computeCompleteLabel(Label label) {
        var labelName = label.getEntityName();
        var labelType = (LabelType) label.eContainer();
        var labelTypeName = labelType.getEntityName();
        return labelTypeName + "." + labelName;
    }

    private void ensureCorrectDFDConversion(DataFlowDiagramAndDictionary complete) {
        var webConverter = new DataFlowDiagramConverter();
        var webBefore = webConverter.dfdToWeb(complete);
        var webAfter = webConverter.dfdToWeb(webConverter.webToDfd(webBefore));
        assertEquals(webBefore, webAfter);
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
