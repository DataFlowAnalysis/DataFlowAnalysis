package org.dataflowanalysis.analysis.tests.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.dataflowanalysis.analysis.converter.DataFlowDiagramAndDictionary;
import org.dataflowanalysis.analysis.converter.DataFlowDiagramConverter;
import org.dataflowanalysis.analysis.converter.webdfd.WebEditorDfd;
import org.dataflowanalysis.dfd.datadictionary.Pin;
import org.dataflowanalysis.dfd.dataflowdiagram.Flow;
import org.dataflowanalysis.dfd.dataflowdiagram.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WebEditorTest extends ConverterTest {
    private DataFlowDiagramConverter converter;

    private final String minimalWebDFD = Paths.get(packagePath, "minimal.json").toString();
    private final String tempWebDFD = Paths.get(packagePath, "test.json").toString();
    private final String minimalDataFlowDiagram = Paths.get(packagePath, "minimal.dataflowdiagram").toString();
    private final String minimalDataDictionary = Paths.get(packagePath, "minimal.datadictionary").toString();

    @BeforeEach
    public void setup() {
        converter = new DataFlowDiagramConverter();
    }

    @Test
    @DisplayName("Test Web -> DFD -> Web")
    public void webToDfdToWeb() throws StreamReadException, DatabindException, IOException {
        DataFlowDiagramAndDictionary dfdBefore = converter.webToDfd(minimalWebDFD);
        WebEditorDfd webAfter = converter.dfdToWeb(dfdBefore);

        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(minimalWebDFD);
        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);

        webBefore.sort();
        webAfter.sort();

        assertEquals(webBefore, webAfter);

        checkBehaviorAndPinNames(dfdBefore);
    }

    @Test
    @DisplayName("Test storing and loading functionality")
    public void testStoreLoad() throws StreamReadException, DatabindException, IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(minimalWebDFD);

        WebEditorDfd webBefore = objectMapper.readValue(file, WebEditorDfd.class);
        DataFlowDiagramAndDictionary completeBefore = converter.webToDfd(webBefore);

        converter.storeWeb(webBefore, tempWebDFD);
        converter.storeDFD(completeBefore, minimalWebDFD);

        WebEditorDfd webAfter = converter.loadWeb(tempWebDFD).get();
        DataFlowDiagramAndDictionary completeAfter = converter.loadDFD(minimalDataFlowDiagram, minimalDataDictionary);

        assertEquals(webBefore, webAfter);
        assertNotNull(completeAfter);

        cleanup(minimalDataFlowDiagram);
        cleanup(minimalDataDictionary);
        cleanup(tempWebDFD);
    }

    @Test
    @DisplayName("Test manual conversion")
    public void testManual() {
        String dataflowdiagram = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "OnlineShopDFD", "onlineshop.dataflowdiagram")
                .toString();
        String datadictionary = Paths.get("..", "org.dataflowanalysis.analysis.testmodels", "models", "OnlineShopDFD", "onlineshop.datadictionary")
                .toString();
        DataFlowDiagramAndDictionary manualDFD = converter.loadDFD(dataflowdiagram, datadictionary);

        DataFlowDiagramAndDictionary convertedDFD = converter.webToDfd(minimalWebDFD);

        assertEquals(manualDFD.dataFlowDiagram().getNodes().size(), convertedDFD.dataFlowDiagram().getNodes().size());

        List<String> nodeEntityNamesManual = manualDFD.dataFlowDiagram().getNodes().stream().map(Node::getEntityName).collect(Collectors.toList());
        Collections.sort(nodeEntityNamesManual);

        List<String> nodeEntityNamesConverted = convertedDFD.dataFlowDiagram().getNodes().stream().map(Node::getEntityName)
                .collect(Collectors.toList());
        Collections.sort(nodeEntityNamesConverted);

        assertEquals(nodeEntityNamesManual, nodeEntityNamesConverted);

        assertEquals(manualDFD.dataFlowDiagram().getFlows().size(), convertedDFD.dataFlowDiagram().getFlows().size());

        assertTrue(manualDFD.dataFlowDiagram().getFlows().stream()
                .allMatch(flowA -> convertedDFD.dataFlowDiagram().getFlows().stream()
                        .anyMatch(flowB -> flowA.getSourceNode().getEntityName().equals(flowB.getSourceNode().getEntityName())
                                && flowA.getDestinationNode().getEntityName().equals(flowB.getDestinationNode().getEntityName()))));

        checkBehaviorAndPinNames(manualDFD);

    }

    private void checkBehaviorAndPinNames(DataFlowDiagramAndDictionary dfd) {
        for (Node node : dfd.dataFlowDiagram().getNodes()) {
            var behaviour = node.getBehaviour();
            assertEquals(node.getEntityName(), behaviour.getEntityName());

            for (Pin inPin : behaviour.getInPin()) {
                String flowName = "";

                for (Flow flow : dfd.dataFlowDiagram().getFlows()) {
                    if (flow.getDestinationPin().equals(inPin)) {
                        flowName = flow.getEntityName();
                    }
                }

                assertEquals(inPin.getEntityName(), node.getEntityName() + "_in_" + flowName);
            }

            for (Pin outPin : behaviour.getOutPin()) {
                String flowName = "";

                for (Flow flow : dfd.dataFlowDiagram().getFlows()) {
                    if (flow.getSourcePin().equals(outPin)) {
                        flowName = flow.getEntityName();
                    }
                }

                assertEquals(outPin.getEntityName(), node.getEntityName() + "_out_" + flowName);
            }
        }
    }
}
